package com.simprints.feature.datagenerator.enrollmentrecords

import android.os.Bundle
import com.simprints.core.DispatcherIO
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.sample.Sample
import com.simprints.core.domain.sample.SampleIdentifier
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.repository.local.models.toDate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.UUID
import javax.inject.Inject

internal class InsertEnrollmentRecordsUseCase @Inject constructor(
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val configRepository: ConfigRepository,
    private val timeHelper: TimeHelper,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
) {
    operator fun invoke(
        projectId: String,
        moduleId: String,
        attendantId: String,
        numRecords: Int,
        templatesPerFormat: Bundle,
        firstSubjectId: String,
        fingerOrder: Bundle?,
    ): Flow<String> = flow {
        val project = configRepository.getProject()
        val tokenizedModuleId = moduleId.asTokenizableEncrypted()
        val tokenizedAttendantId = attendantId.asTokenizableEncrypted()
        val creationDate = timeHelper.now().ms.toDate()
        val updateDate = System.currentTimeMillis().toDate()
        // generate n records and insert them in the repo the first subjectId is used for only one record the rest are generated randomly
        var subjectCreationActions = mutableListOf<SubjectAction.Creation>()
        for (i in 0 until numRecords) {
            val subjectId = if (i == 0 && firstSubjectId.isNotBlank()) firstSubjectId else UUID.randomUUID().toString()
            val subject = Subject(
                subjectId = subjectId,
                projectId = projectId,
                attendantId = tokenizedAttendantId,
                moduleId = tokenizedModuleId,
                createdAt = creationDate,
                updatedAt = updateDate,
                samples = generateFingerprintTemplates(
                    templatesPerFormat = templatesPerFormat,
                    fingerOrder = fingerOrder,
                ) + generateFaceSamples(
                    templatesPerFormat = templatesPerFormat,
                ),
            )
            subjectCreationActions.add(SubjectAction.Creation(subject))
            if (subjectCreationActions.size >= BATCH_SIZE) {
                emit("Inserted ${i + 1} biometric records")
                enrolmentRecordRepository.performActions(
                    subjectCreationActions,
                    project,
                )
                subjectCreationActions = mutableListOf()
            }
        }
        if (subjectCreationActions.isNotEmpty()) {
            enrolmentRecordRepository.performActions(
                subjectCreationActions,
                project,
            )
        }
        emit("Inserted $numRecords biometric records")
    }.flowOn(dispatcher)

    private fun generateFaceSamples(templatesPerFormat: Bundle): List<Sample> {
        val faceSamples = mutableListOf<Sample>()
        for (key in templatesPerFormat.keySet()) {
            if (FINGERPRINT_FORMATES.contains(key)) {
                // Skip non-face formats
                continue
            }
            val numSamples = templatesPerFormat.getInt(key, 0)
            repeat(numSamples) {
                faceSamples.add(
                    Sample(
                        template = getTemplateForFormat(key),
                        format = key,
                        referenceId = UUID.randomUUID().toString(),
                        id = UUID.randomUUID().toString(),
                        modality = Modality.FACE,
                    ),
                )
            }
        }
        return faceSamples
    }

    private fun generateFingerprintTemplates(
        templatesPerFormat: Bundle,
        fingerOrder: Bundle?,
    ): List<Sample> {
        val fingerprintSamples = mutableListOf<Sample>()

        for (key in templatesPerFormat.keySet()) {
            if (FACE_FORMATES.contains(key)) {
                // Skip face formats as they are not fingerprint templates
                continue
            }
            // finger order is a comma separated string of finger identifiers
            val fingerIdentifiers = fingerOrder?.getString(key, "")?.split(",")
            val numSamples = templatesPerFormat.getInt(key, 0)
            for (i in 0 until numSamples) {
                fingerprintSamples.add(
                    Sample(
                        template = getTemplateForFormat(key),
                        format = key,
                        referenceId = UUID.randomUUID().toString(),
                        id = UUID.randomUUID().toString(),
                        identifier = if (fingerIdentifiers.isNullOrEmpty()) {
                            SampleIdentifier.LEFT_THUMB
                        } else {
                            fingerIdentifiers[i % fingerIdentifiers.size].toFingerIdentifier()
                        },
                        modality = Modality.FINGERPRINT,
                    ),
                )
            }
        }
        return fingerprintSamples
    }

    private fun String.toFingerIdentifier() = when (this.uppercase()) {
        "LEFT_THUMB" -> {
            SampleIdentifier.LEFT_THUMB
        }

        "LEFT_INDEX_FINGER" -> {
            SampleIdentifier.LEFT_INDEX_FINGER
        }

        "LEFT_3RD_FINGER" -> {
            SampleIdentifier.LEFT_3RD_FINGER
        }

        "LEFT_4TH_FINGER" -> {
            SampleIdentifier.LEFT_4TH_FINGER
        }

        "LEFT_5TH_FINGER" -> {
            SampleIdentifier.LEFT_5TH_FINGER
        }

        "RIGHT_THUMB" -> {
            SampleIdentifier.RIGHT_THUMB
        }

        "RIGHT_INDEX_FINGER" -> {
            SampleIdentifier.RIGHT_INDEX_FINGER
        }

        "RIGHT_3RD_FINGER" -> {
            SampleIdentifier.RIGHT_3RD_FINGER
        }

        "RIGHT_4TH_FINGER" -> {
            SampleIdentifier.RIGHT_4TH_FINGER
        }

        "RIGHT_5TH_FINGER" -> {
            SampleIdentifier.RIGHT_5TH_FINGER
        }

        else -> {
            SampleIdentifier.LEFT_THUMB
        }
    }

    private fun getTemplateForFormat(format: String): ByteArray = when (format) {
        "SIM_FACE_BASE_1" -> SIM_FACE_BASE_1
        "RANK_ONE_1_23" -> RANK_ONE_1_23
        "RANK_ONE_3_1" -> RANK_ONE_3_1
        "ISO_19794_2" -> ISO_19794_2
        "NEC_1_5" -> NEC_1_5
        else -> ByteArray(0)
    }

    @OptIn(ExperimentalStdlibApi::class)
    companion object {
        const val BATCH_SIZE = 500

        // const for all templates byte arrays
        val SIM_FACE_BASE_1 =
            """
            b485cf3dfec8423ecce9163edae117bde5e866be52c8e3bcfc3037bd2b641fbd58b715beed08053ef88e653e800059bdb4e03bbd9700c53d6a67853ed59b93bd4c31413daf1653bd0b0902bd305231bcb82405bbfc411a3d38b18c3d13168d3d67cb6cbe3bd18f3d44da723d96be3d3d7666f3bc059203be2c1c2cbea685ac3c8e51f8bd5cc18abd7213dfbd38849c3d4c1e673e2af0f93d1fc68fbbfe729b3dce889ebde595f73d82d8783cf7390a3e1c8cac3c5e142a3e60abe4ba0fcfe6bd603022bec8c181bdefe906be4e9ca13c4e6f22bd6c96d9bc0a3e49bddc42413ea4a4973dd88cec3dd6e4c5bda4abbabd56e8ad3d94f40fbb8143923e805ed2bdce22823ed4a360be0ad619bd60a0a4bd2568be3db0a30bbba47fe8bc34059b3dca060cbe370e643d300ea93ec912143eccdd65bd24f1393da6a1c1bc886c7c3def33e63cf16817be58b177bd2ef10a3e8e528ebdb064843dfa1dd73ddcb7ca3cfd65aabdb369bf3c385142bcd4b34cbe18ac993dca7243be328dc03d6306353dd2dd08be6676f23dbaed7cbd50bfa1bb8040643d2850603dd01525bb6f71a63d54e402bb48d5a03dd4cd153e603da2bdf2b09abd0c9de23cf25eb9bd26ba983e208ec43b714454be06d754be089c803dba00573dd5f70bbe3bde143ebe81abbde73d093dd830493d8458193e1244bb3d2071393b44ab2c3d8653a0bd53d6a03d58b3dc3b6ee6493ec368803caa982dbd4f219b3d23652f3ea5668cbdfa6dde3d1b8803bd50ea813e3c5265bd8200553e4480f33b9ac7d23cfcb4113df626f4bde72bb4bd9e0921bdb74a2a3ec67876becc3838be455c08be54a1563c5073cdbde0005b3a0654883da09cc7bd285fe9bc1fcd5f3ed05ee93dd2fa913dc48dea3de650473e46e3e9bc5c4a353ddb1f853d0a5221be902350beb4c02f3d90a7d33d02c36fbdc7d4f53df82f5ebd0eaf553d577dbbbdfa6a4a3e3a51663d2b842e3e630adebd0081ee3df606cbbcc2822c3dd094ccbb2e1f67bec54e883ea417573d93c38d3d39af38bec977a6bda3202bbcc5e9073d4c98a33ce00eab3dc2c2f33df4bfc53dc5c5373ef05f943c9844eebb71fe9bbdec3f993dc03dc2bd19955dbe86075fbed0ded7bab3d0b13d865010bd2420783e29768d3d0f97203e54317fbda6f38abdf65dc6bd2896f33b72e6913c09462a3ecc0021be4e96e63dbcdbd83d2ee7a43cf1e3fabddc030e3eaceb7cbc98b8813d841fa9bda666fbbd89accebe25b09e3d2c9d92bc834f21bd6094493ddf4261be1a1112bdd2c95a3dc677b33d081d11be024e73bd44e80e3ebe9b86bd7481dcbda93f073e6a4a15bdafcbf93cae16be3df4477abd496a0fbe38c1053c6ce1053e9c9707bd8cf5b5bd3fdc40be507133bdfb844fbe2edc8b3dc4936e3c7ff3dbbd8c62f13d792441be30b4b23d3cdf97bdbe70703d26fd063eb036bf3db0182b3d3e581cbe70ecfdbdd8f483bb4fa0c33d792a3c3e7a2bde3dd0f04c3c2ea88bbecf7e4bbdfc281f3e2e007c3d9c50a93d367c32be5ad514bcdd70c03db752053daaa3d6bd45e326bddb2f5dbd0da7883da4e5323dc3610bbd6a2631bea52b97bd1bce343dc811d23d4d67d7bdf4ace6bcc34c6dbd168172bebce668bda4f1543e8e4ffc3d33ae99bec294afbd72d546bdb5682a3ecdf4a23d8af64abeb96dc13d00c647be0d6455beb753d6bdaf4095bde26a023d12b3063ea8422b3e92dd82bcb57a953d927cf03ceb0ec33d4aa5e3bc90d74bbb7c4501bd58a0983c8e39a4bdb787e83df559b33c4d911c3e424b763d60ed8ebd3656fcbc44c2203d223386bee11c7bbe8cdda93d8e4ec33cae8a83bdf08ed7bd9ed60b3d6f27043ec274d3bc3952c23c56b7a13d2805b83c90eec83b96afd93dcfe437be3e0288bdb8529b3d48dea43d537a473e8a97f03d39fd543da2b2853ea093443d1cd9343cff7b41bdf13440bdb580d0bd741d203e6cd09ebd48de223d76d2b73c54038a3c16cdd3bd4a4f1d3eaacd2a3ec068fcbd81fba03dc8063f3e2f8636bec0779d3d5939d9bd36d139bde092023d49a1c33c06f208be4c221c3db311693eea1a9c3deddeb63d1f04b83d534172bd7174a4bd5eb1eebcb553293dae6f34bde9dc4cbe2140afbb1f749e3c34ad31be287aa63c5f6d8dbd72a0ca3d04711e3dc0b6deba683fc03b15501abd80685eba902809bc8854f93d5604cfbc42fe353e54a249be768f373e0477ad3bda4ee53d9934c9bd562086bdd02d8ebdfa8d82be9fb806bd941a5abe16442c3d42bb0dbe14a8f73c04abc03cb8ebf73d50ea6ebd30c51dbd316a88bdbc5008beb84d75bd5ac5c1bd0598f3bd6684973d3839543b86e4963da03593bda0348a3d08ad363e3c2c923e7e20af3deed79fbdf384363eb313833d07f90bbc22a0293c2e0328bdd675363e6c342cbd1d7f00be585111bd0081a93c316696bd0556d7bdb7a653bd841bb93d9bd1dbbc52c92fbbfc54ea3c80dd043e8439ff3d50ca99bd8c3bb23bf8cccabdd08cb4bbc8515abb9c58273edfda373dac316f3d609fddbd8a3513bc4b2fa0bd8a3b063de87f093e4ecf3e3e56cbac3db8a226be88ef95be78816cbcc27c8e3df07dac3d657b3bbdaced13bec30a0dbe12382b3ecb8b0c3e84e9f33c22bf4a3d6bccd4bd5e21c6bd4c2825be932b253e3ed5b13d46d14cbd0807d3bd52145bbd460308be2a1b723ddd84883d579d923dba6b153e9b11e33da8a785bd08d54ebd52fc8e3df51814be194d03bd9eba743e25b611bee80497bdb897843dac3db23de1439ebd1bd02f3dcfdf43bded9618be9562f1bc04b1d6bcc2e5113eeee1083eefb3513d28a91d3eb4ea75bcf445033e82ba3abdf6548d3d573a04bd337fa2bd
            """.trimIndent().hexToByteArray()
        val RANK_ONE_1_23 =
            """
            022c072f0f6023f423f000d4f4cff30e6d237f30e2f030fb211123e040bed3e3e1d321431fcf202d0f00c22f632151de2df3b1ffe1eff05ed11dce20e2e3ed0c6eeedae324d0201204adf2e403f03ffe302cf32ced592f0214dcde22321000db2f50fff0004e74d100ef22aae2dedd01123e03ce33e021255210313ed0e0e52e12eb0f1fd2fe2de01d53312de91f43d3ee0ff2ec30e25e11c3cee31302e1e2529f12d304c5491f003fbd10fe1b111b2123f0e0f005d4213c40526efe503c120ed1f15f12eb70f20f2e22ff31d2cf3c6e23fb6de0d120011f11f30dd3dfe24a0412bf003f321dfcdf1d139b1f32a375fd2f0402c0f1ffd4d6290ff12e4c0d3fd0010060f3c4
            """.trimIndent().hexToByteArray()
        val RANK_ONE_3_1 =
            """
            060000004a1a122fe2f1a1102faef40f0720eec301c11a011012f1224fee1f2e1f363ef2e31f3f001cefd011dff1e3402eef4e01cec3f0712e044911d02fbdcfffde114c135bc15323d1212e31a1f13209c2d232a0e101300c1c12f0f3bd30ff0ce0ec401d4b0ad1ce0fd2f39f2d0323fd3e2e13141d52a1f12fc1dcd44ff10fff0fe15220321fe1313d3ae34ce10d2e2033d50c721012d0f01040145f1eed41bdc5c1d4e62eed9d1f5d0f0f33e401f1060e01d10e1f0113e3e23f0e230dbd09fae10f0611f11043221c5cfe206ed00d5f1243cecc2235fdc32d1fdcf2111e9fd30d03c2a2f00d2ee3d0cd3cf1d1101f40f11dd3132edf24f0c3d1ef104efe1e31c13e0f00e0eac4
            """.trimIndent().hexToByteArray()
        val ISO_19794_2 =
            """
            464d5200203230000000010e0000012c019000c500c5010000106328409c002f150040d1003c0d0040ab00410d00402400521b0040450057150040ce005c0600406c005c9400407d00610d0040570067140040da0071fb0040ed008cf800806e008f0b0040230094140040ae00b58300807c00b8090040bd00cefe0040f100d67500404f00e7980040f200f0f10040d500f2ef0040a800fcfb00807e011e070040a00124f50040da0125ea0040ad0135e70040860144fe0041220148d700404a0158b800401c015db100409a015fc600408a0161b300807e0164b10040d40169ca0080b10169c50040f30170c70040e50171c60041000172ca00407c017438004059017ad6008071017f68000000
            """.trimIndent().hexToByteArray()
        val NEC_1_5 =
            """
            4c464d4c00090000494d000c408900000000000003fc05504c464d4c00060000504900304085cd653594d653008b00c64085cd653594d653008b00c64085cd653594d653008b00c6407c1b0cd0000000ffdbfffb4c464d4c00040000455600401a121d3a0054810042000000000000000e0e162d00324b00600000000000000014000000000000005100000000000000112262af003a7c005e000000000000004c464d4c000100004d540028000002d900000000000002d9019c000000000000047503b7000000000000082c026700000000000050433200413100050001e000405a320066011a011e05e600037fff819ffff0e3fffc79ff789e7fdeffffff9fff9ff7ffefffcbfffff2ffffffbfffffffffffefffffe0f9fff80c7ffec03ffffdefffffffffffeffffff8ffffffffffffffffffffffffffffffdffffff3fffffc07fdff00fcffc00c03e04d3200d81532905711f534ec1501ac630429e768019289fc042edee815259f5914936ce210dac0ee10cacbed155289d611da9e6c1438bc6811d939ee05755c5a11a8d6f8155080da11c7bc730193807c057e4eda047fa061158241dd11da70ed042e5466158a5e5911849d081511a05e1431b268055303e3104e91f400d69d6a105cad75019baefd00d36de91074a37111803f7b106f897010e24a6714867ee100c8766a146d4760054d0a641145858d00e2abee019abf84048396dd0586815911db53ed058984d911edb668042560e41469a86310e4396511e2af6b563100361200f001fd0e02000000ef02100000dd0e103000f0001eee01ed01010e0f0e01fd00001020000f002010102e32102e0e0e1f210000105231014459a1bffca3a938320936f5802f1ffffdaa562015a9ff316b5928ffa99811ff332b20d0dc59affffca001ffffe85db9bf6f2ae48890430c87a48a3341fee390040fff811ff508e5a3acd15cff35db0432628d2bda05a42efd84e9c51b2afffffffee3a9040fcab0ffffd4b584a8480b58d8ff63f5690ef3aecb3718fffb68fff1dca5a4d8029be34842c29726f39a8fff4ce2aeffffd511276afff811582ffffc6a6fac914ccd4ca1fffcc50c1a55934ffffff7e28aa22b6f2867fff8cd8a477f2d6119fffb11c9797ffe77ab7ff8c97a791dfea4af3497ff75f8899e5ffffe485bcbfd3ffff1e85ccb7f92221eca197fd17ce61fffff201415fdccc3fffb198dffff6f285e889205fff3d12e066afff9a85f351504afffc3ffd84013628d259affffd8b8afffd4fffffc2bcb9a85ff96121bacc715fd49b2fe495bcbffff811ff109a58b454d000050433200413100050001e000405a320063011a021e00000c00000383000021a000007100003ee0000f380003cf000037c00018f90004bef801dfff807f87f01f83fc07fb4701fce0f05fbc1f15ff0fc47ff8f1d57f987e07f60f81ff03e1ffc8fc7ff63e1fff9f85ffefc078fbf0037f000003804d32007011c33fee11cc32ee1541b1501148a8951509b363058443dc016b42801158b19611b64af211da9e6d05834dd815548cd51536be541145858c01859c07154f7fd911399f95117e077701a858f70572615711872d77107ee67601cd536f11884079154d09631123d2aa11e3a96c151eb95e5631001c0e000010000e0210ff020fef0010ed0e2000000e000011201130111f5231008c787f1ba136c7ff1a02c4b6d9fffd6fcfcee3361fd67ffdaf16fffffc595ffb5d2bc7ffcffee9347f783f4bc93677ffa24ed2fffe5c5a7fffe6fb105f4efffffc58fc0ee347ffb48cd3ffe9ffe78b684bcfc6e6fffff9a88c7ebfb22376bbe90e9ffffd58f9af20fe3ffb621c7fff439247e020335d207e9f885e5ffffffff4387fff77fff23ec921fff609f6454d000050433200413100050000d8f8405a320066011a011e05e600037fff819ffff0e3fffc79ff789e7fdeffffff9fff9ff7ffefffcffffff6ffffffbfffffffffffffffffe7f9fff9fefffeff3fffffeffffffffffffffffffdffffffffffffffffffffffffffffffdffffff7fffffff7fffffcfcffc00c03e04d3201101505af6315538ad511da9e6c15735e58154f7fd905804dd9158342dc11849c0711843f7a054d09631145858c11e2ac6b1532905711f534ec0429e768019289fc042edee815259f5914936ce210dac0ee10cacbed1438bc6811d939ee11a8d6f811c7bc730193807c047fa06111da70ed042e5466058a5e591511a05e1431b268055303e3104e91f400d69d6a105cad75019baefd00d36de91074a371106f897010e24a6714867ee100c8766a146d476000e2abee019abf84048396dd01db53ed11edb668042560e41469a86310e4396511c33fee11cc32ee1541b1501148a895116b42801158b19611b64af21536be5411399f95117e077711a858f711872d77107ee67601cd536f1123d2aa151eb95e56310044f0ef020f0e0f0fed20111011120001fd0e020000001000dd1030f01eee0101010e0f0e01fd000020000f00202e32100e1f2100000e0000100210ff00200000000011301f523101dc16c3fff8644f1bfffc1841a31b7f4863f8b000cdbe1abc27ffe74289fe741891a31b01fffc32012bc19dfffffa012bc29d1e65c0a73fd2187028cbe68fdeff2634206fe8190efef1c8770f7fff4863fb00030d8236788b6fe0418906389942cbcd8c2634d8940c882c366447f721d218fec8aac2658720367a1bb6c0460dfffffab387d4ff3ec56274c51a8e1c4d1274d01a8d5858820fec8e1f1a38a3568e979e60616b985fffff5a5ffff602cd8f143d20236f847f62c9006fffd72b18257c17c3c11e50c4fb252affff120c0fffffff8536c004376444af585081564698a5fffd9101c242064ecfffffffd1a64d009ac3ec52aafffdec04c9d0ff04f878773fd698627147ffea912c47fe01444a93a1428d31454a3ffffe9d29f63fd9fffffc92b385577f4e989a2629547f667192925fffff3ec52a24c5099085fcb9804903b9fffd4ab38c8d060834b348264d9e305ffff860178b52ace7f509499f5855006fe694b1fe9d41f63fffff2cd4a3d7ee81411a38a3d7ed2096230ddfffffc08485fdc877029e7ffe28cbe10f51ff85fcb871e29bff5ed0a3f10f8141230fffffffdb0c85fdb87148fffffee1fba1c5207f82e9e88146521bfee043e6a51efffe29eb528891ff746411b5e59ab4fffd6b779fffff0063fff76d889e454d000050433200413100050000c0f0405a32004c0116031c00880003fa003ff801ffe01fffe07fffc1ffff8ffffe1ffff8ffffe3ffffdffffdfffff7fffff3ffffcfffff3ffff9fffff7ffffdfffff3efffc7ffff3ffffc7fffe1ffff80b7f804d3200b8118e137b114382b410739c7311cbab6b1069be7d11f9bb75114e9a4f11dfa6eb014e73a6150e6d5411f4a669119035811508bf7410a7ca70153ab26b154243561513925d1195438010d7ae6c15133fdb01567a3601a47deb152bb06b1172b75e01646ba41406bc68019686cf013172a61184699b11f3695e016d6ca11021f695016a763111929e580524c6781161449011d7586a10d47a67153618dc11a5648710ba856c055cb86c11cc256f10e5fe6b143ab862013691c85631002e212012ee2110fe122200103200101f20ef32000eddc30e0e221f431130202122dded2e22201e0f3200f22020003252310114fe32eaacd11f1975087dbfedfdf10da3ffff87ffff1d09157c800da02fff5fffff31028706dfff5e18142907550fffc50548188df6ed91d2903bf3d31fffc55907648dc464a8d01f17ffff88f40a12302b652a025a2fffa7f3d0ad324effffe690a3163bf3c98dc9e4a8d2df08dae667f968fcf9abaa47498c80465e18187649dc79a84314c8bf3bf3d01bffff3216a01a07918dffd4480ae6b3ffe50c75677205e1fc12237fffff19a5674408de9151d024f4ea82169c2e3614fdb8c013ffff7985065e169c8065ff0c755a5fffffa5640c6d870b0047ff9d51dd253a80fe8499b3ffffffffecaea4cf8150dd92a45cfc2352b25fff3bfffffff3c92d191d4eb48d02a9139ac64d49998ffffffffc605bffffff454d0000
            """.trimIndent().hexToByteArray()
        val FACE_FORMATES = setOf("SIM_FACE_BASE_1", "RANK_ONE_1_23", "RANK_ONE_3_1")
        val FINGERPRINT_FORMATES = setOf("ISO_19794_2", "NEC_1_5")
    }
}
