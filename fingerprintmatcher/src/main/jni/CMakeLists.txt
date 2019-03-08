cmake_minimum_required(VERSION 3.6)
project(SimMatcherWrapper)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_SOURCE_DIR}")

set(LIB_SOURCE_FILES
        SimMatcher/src/afm-src/Angle.c
        SimMatcher/src/afm-src/Angle.h
        SimMatcher/src/afm-src/Minutia.c
        SimMatcher/src/afm-src/Minutia.h
        SimMatcher/src/afm-src/Edge.c
        SimMatcher/src/afm-src/Edge.h
        SimMatcher/src/afm-src/Template.c
        SimMatcher/src/afm-src/Template.h
        SimMatcher/src/afm-src/Pair.c
        SimMatcher/src/afm-src/Pair.h
        SimMatcher/src/afm-src/Pairing.c
        SimMatcher/src/afm-src/Pairing.h
        SimMatcher/src/afm-src/List.c
        SimMatcher/src/afm-src/List.h
        SimMatcher/src/afm-src/match.c
        SimMatcher/src/afm-src/match.h
        SimMatcher/src/afm-src/afm.h
        SimMatcher/src/afm-src/afm.c
        SimMatcher/src/afm-src/environment.h
        SimMatcher/src/afm-src/utils.h
        afm-jni.c)

add_library(simmatcherwrapper SHARED ${LIB_SOURCE_FILES})
target_link_libraries(simmatcherwrapper log m)