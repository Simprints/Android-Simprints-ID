#include <jni.h>
#include <android/log.h>

#include <stddef.h>
#include <malloc.h>
#include <unistd.h>
#include <math.h>
#include <pthread.h>

#include "SimMatcher/src/afm-src/afm.h"

#define APPNAME "SimAFIS"

#define LOG(args...) \
    __android_log_print(ANDROID_LOG_DEBUG, APPNAME, args)\


#define ASSERT_NO_JNI_EXCEPTION(env) \
  if ((*env)->ExceptionCheck(env)) {\
    (*env)->ExceptionDescribe(env);\
    (*env)->ExceptionClear(env);\
    LOG("No jni exception");\
    return JNI_FALSE;\
  }\

#define ASSERT_NON_NULL(pointer) \
  if (pointer == NULL) {\
    LOG("null pointer exception");\
    return JNI_FALSE;\
  }\

#define ENV(methodName, args...)\
    (*env)->methodName(env, args)\

#define DEFINE_GLOBAL_CLASS(name, path) \
    LOG("Defining global class %s", path);\
    jclass local_##name = ENV(FindClass, path);\
    ASSERT_NO_JNI_EXCEPTION( env )\
    name = ENV(NewGlobalRef, local_##name);\
    ASSERT_NON_NULL(name)\
    ENV(DeleteLocalRef, local_##name)\

#define DEFINE_GLOBAL_METHOD(name, class, methodName, methodSignature) \
    LOG("Defining global method %s", methodName);\
    name = ENV(GetMethodID, class, methodName, methodSignature);\
    ASSERT_NO_JNI_EXCEPTION( env )\

#define DEFINE_GLOBAL_STATIC_METHOD(name, class, methodName, methodSignature) \
    LOG("Defining global static method %s", methodName);\
    name = ENV(GetStaticMethodID, class, methodName, methodSignature);\
    ASSERT_NO_JNI_EXCEPTION( env )\

#define DEFINE_GLOBAL_OBJECT_ARRAY(name, value) \
    LOG("Defining global object array");\
    jobjectArray local_##name = value;\
    ASSERT_NO_JNI_EXCEPTION( env )\
    name = ENV(NewGlobalRef, local_##name);\
    ASSERT_NON_NULL(name)\
    ENV(DeleteLocalRef, local_##name)\



// global variables cached for performance

jclass c_List;
jclass c_Person;
jclass c_Fingerprint;
jclass c_FingerIdentifier;

jmethodID m_size;
jmethodID m_get;
jmethodID m_getFingerprints;
jmethodID m_getDirectBufferTemplate;
jmethodID m_getFingerIdentifier;
jmethodID m_values;
jmethodID m_ordinal;

jobjectArray oa_fingerIdentifiers;

JavaVM *jvm;


// Resolve and cache once all the jclass and jmethodId used later
JNIEXPORT jboolean JNICALL Java_com_simprints_fingerprintmatcher_JNILibAfis_nativeInit(
        JNIEnv* env,
        jobject obj)
{
    LOG("nativeInit() Initializing classes");
    DEFINE_GLOBAL_CLASS(c_List, "java/util/List");
    DEFINE_GLOBAL_CLASS(c_Person, "com/simprints/libcommon/Person");
    DEFINE_GLOBAL_CLASS(c_Fingerprint, "com/simprints/libcommon/Fingerprint");
    DEFINE_GLOBAL_CLASS(c_FingerIdentifier, "com/simprints/libsimprints/FingerIdentifier");

    LOG("nativeInit() Initializing methods");
    DEFINE_GLOBAL_METHOD(m_size, c_List, "size", "()I");
    DEFINE_GLOBAL_METHOD(m_get, c_List, "get", "(I)Ljava/lang/Object;");

    DEFINE_GLOBAL_METHOD(m_getFingerprints, c_Person, "getFingerprints", "()Ljava/util/List;");

    DEFINE_GLOBAL_METHOD(m_getFingerIdentifier, c_Fingerprint, "getFingerId", "()Lcom/simprints/libsimprints/FingerIdentifier;");
    DEFINE_GLOBAL_METHOD(m_getDirectBufferTemplate, c_Fingerprint, "getTemplateDirectBuffer", "()Ljava/nio/ByteBuffer;");

    DEFINE_GLOBAL_STATIC_METHOD(m_values, c_FingerIdentifier, "values", "()[Lcom/simprints/libsimprints/FingerIdentifier;");
    DEFINE_GLOBAL_METHOD(m_ordinal, c_FingerIdentifier, "ordinal", "()I");

    LOG("nativeInit() Initializing objects");
    DEFINE_GLOBAL_OBJECT_ARRAY(oa_fingerIdentifiers, ENV(CallStaticObjectMethod, c_FingerIdentifier, m_values));

    return JNI_TRUE;
}

JNIEXPORT jint JNICALL Java_com_simprints_fingerprintmatcher_JNILibAfis_getNbCores(
        JNIEnv *env,
        jobject obj)
{
    return (int)sysconf(_SC_NPROCESSORS_ONLN);
}


JNIEXPORT jfloat JNICALL Java_com_simprints_fingerprintmatcher_JNILibAfis_verify(
        JNIEnv* env,
        jobject obj,
        jobject probe,
        jobject candidate)
{
    jbyte* probeBuffer = ENV(GetDirectBufferAddress, probe);
    jbyte* candidateBuffer = ENV(GetDirectBufferAddress, candidate);
    LOG("verify()");

    float result;
    int returnCode = verify(&result, (char *)probeBuffer, (char *)candidateBuffer);
    LOG("verify() returning result %.3f (returnCode %d)", result, returnCode);

    return result;
}

typedef struct matchingTask {
    jobject probe;
    jobject candidates;
    // array where to write matching scores
    float *scores;
    // first candidate to handle
    int start;
    // first candidate not to handle
    int end;
} matchingTask;

void* performMatchingTask(void *arg)
{
    matchingTask *task = (matchingTask *)arg;
    JNIEnv *env;
    (*jvm)->AttachCurrentThread(jvm, &env, NULL);

    int nbCandidates = task->end - task->start;

    // Get pointers on the template buffers of the probe
    int nbFingerIds = ENV(GetArrayLength, oa_fingerIdentifiers);
    jbyte **probeTemplates = calloc((size_t)nbFingerIds, sizeof(jbyte *));

    jobject probePrints = ENV(CallObjectMethod, task->probe, m_getFingerprints);
    int nbProbePrints = ENV(CallIntMethod, probePrints, m_size);
    LOG("identify() probe has %d fingerprints", nbProbePrints);

    for (int printNo = 0; printNo < nbProbePrints; printNo++) {
        jobject print = ENV(CallObjectMethod, probePrints, m_get, printNo);
        jobject fingerId = ENV(CallObjectMethod, print, m_getFingerIdentifier);
        int fingerNo = ENV(CallIntMethod, fingerId, m_ordinal);
        jobject templateDirectBuffer = ENV(CallObjectMethod, print, m_getDirectBufferTemplate);
        probeTemplates[fingerNo] = ENV(GetDirectBufferAddress, templateDirectBuffer);
        ENV(DeleteLocalRef, print);
        ENV(DeleteLocalRef, fingerId);
        ENV(DeleteLocalRef, templateDirectBuffer);
    }
    ENV(DeleteLocalRef, probePrints);
    LOG("identify() loaded probe templates");

    // Get pointers on the template buffers of the candidates, for the fingers that
    // the probe has

    // contains all the candidate templates, sorted by finger
    jbyte ***candidatesTemplates = malloc(nbFingerIds * sizeof(jbyte **));
    // contains the candidateNo corresponding to the templates stored in the array above
    int **templatesOwners = malloc(nbFingerIds * sizeof(int *));
    // contains the number of candidate templates for each finger that the probe has
    int *nbCandidatesTemplates = calloc((size_t)nbFingerIds, sizeof(int));

    for (int fingerNo = 0; fingerNo < nbFingerIds; fingerNo++)
    {
        if (probeTemplates[fingerNo] != NULL)
        {
            candidatesTemplates[fingerNo] = malloc(nbCandidates * sizeof(jbyte *));
            templatesOwners[fingerNo] = malloc(nbCandidates * sizeof(int));
        }
    }

    for (int candidateNo = task->start; candidateNo < task->end; candidateNo++)
    {
        jobject candidate = ENV(CallObjectMethod, task->candidates, m_get, candidateNo);
        jobject prints = ENV(CallObjectMethod, candidate, m_getFingerprints);
        int nbPrints = ENV(CallIntMethod, prints, m_size);

        for (int printNo = 0; printNo < nbPrints; printNo++)
        {
            jobject print = ENV(CallObjectMethod, prints, m_get, printNo);
            jobject fingerId = ENV(CallObjectMethod, print, m_getFingerIdentifier);
            int fingerNo = ENV(CallIntMethod, fingerId, m_ordinal);

            if (probeTemplates[fingerNo] != NULL)
            {
                int nextPrintIndex = nbCandidatesTemplates[fingerNo];
                templatesOwners[fingerNo][nextPrintIndex] = candidateNo;
                jobject templateDirectBuffer = ENV(CallObjectMethod, print, m_getDirectBufferTemplate);
                candidatesTemplates[fingerNo][nextPrintIndex] = ENV(GetDirectBufferAddress, templateDirectBuffer);
                nbCandidatesTemplates[fingerNo]++;
                ENV(DeleteLocalRef, templateDirectBuffer);
            }
            ENV(DeleteLocalRef, print);
            ENV(DeleteLocalRef, fingerId);
        }

        ENV(DeleteLocalRef, candidate);
        ENV(DeleteLocalRef, prints);
        LOG("identify() loaded candidate %d 's template", candidateNo);

    }

    // Call libAfis identify method for each finger of the probe

    // Used to store temporarily the number of finger a candidate has in common with the probe
    // in order to be able to compute the average score of each candidate
    int *nbSubScores = calloc((size_t)nbCandidates, sizeof(int));
    // Space for the matcher Results
    float *results = calloc((size_t)nbCandidates, sizeof(float));

    for (int fingerNo = 0; fingerNo < nbFingerIds; fingerNo++)
    {
        if (probeTemplates[fingerNo] != NULL && nbCandidatesTemplates[fingerNo] > 0)
        {
            int resultCode = identify(
                    results,
                    (char *)probeTemplates[fingerNo],
                    (char **)candidatesTemplates[fingerNo],
                    (uint32_t)nbCandidatesTemplates[fingerNo]
            );
            LOG("identify() called LibAFIS identify method for finger no %d, with %d candidates : resultCode %d",
                fingerNo, nbCandidatesTemplates[fingerNo], resultCode);

            // Extract scores
            for (int printNo = 0; printNo < nbCandidatesTemplates[fingerNo]; printNo++)
            {
                int candidateNo = templatesOwners[fingerNo][printNo];
                task->scores[candidateNo] += results[printNo];
                nbSubScores[candidateNo-task->start]++;
            }
        }
    }

    // prepare result
    for (int candidateNo = task->start; candidateNo < task->end; candidateNo++) {
        if (nbSubScores[candidateNo - task->start] > 0)
        {
            task->scores[candidateNo] /= nbSubScores[candidateNo - task->start];
        }
    }

    // release resources
    free(nbSubScores);
    free(results);

    for (int fingerNo = 0; fingerNo < nbFingerIds; fingerNo++) {
        if (probeTemplates[fingerNo] != NULL)
        {
            free(candidatesTemplates[fingerNo]);
            free(templatesOwners[fingerNo]);
        }
    }

    free(candidatesTemplates);
    free(templatesOwners);
    free(nbCandidatesTemplates);
    free(probeTemplates);

    (*jvm)->DetachCurrentThread(jvm);

    return NULL;
}

JNIEXPORT jfloatArray JNICALL Java_com_simprints_fingerprintmatcher_JNILibAfis_identify(
        JNIEnv* env,
        jobject obj,
        jobject probe,
        jobject candidates,
        jint nbThreads)
{
    ENV(GetJavaVM, &jvm);

    int nbCandidates = ENV(CallIntMethod, candidates, m_size);
    LOG("identify() matching probe against %d candidates", nbCandidates);

    jobject globalProbe = ENV(NewGlobalRef, probe);
    jobject globalCandidates = ENV(NewGlobalRef, candidates);

    float *scores = calloc((size_t)nbCandidates, sizeof(float));

    matchingTask tasks[nbThreads];
    pthread_t threads[nbThreads];

    for (int threadNo = 0; threadNo < nbThreads; threadNo++) {
        tasks[threadNo].probe = globalProbe;
        tasks[threadNo].candidates = globalCandidates;
        tasks[threadNo].scores = scores;
        tasks[threadNo].start = (int)round((float)threadNo * (float)nbCandidates / (float)nbThreads);
        tasks[threadNo].end = (int)round((float)(threadNo+1) * (float)nbCandidates / (float)nbThreads);

        int err = pthread_create(threads+threadNo, NULL, &performMatchingTask, tasks+threadNo);
        if (err != 0) {
            LOG("Error creating thread number %d: %s", threadNo, strerror(err));
        }
    }

    for (int threadNo = 0; threadNo < nbThreads; threadNo++) {
        int err = pthread_join(threads[threadNo], NULL);
        if (err != 0) {
            LOG("Error joining thread number %d: %s", threadNo, strerror(err));
        }
    }


    jfloatArray results = ENV(NewFloatArray, nbCandidates);
    ENV(SetFloatArrayRegion, results, 0, nbCandidates, scores);

    // release resources
    free(scores);
    ENV(DeleteGlobalRef, globalProbe);
    ENV(DeleteGlobalRef, globalCandidates);

    return results;
}


