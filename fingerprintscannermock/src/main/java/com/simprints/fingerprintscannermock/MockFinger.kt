package com.simprints.fingerprintscannermock


enum class MockFinger(val imageQualityResponse: String = "fa fa fa fa 0e 00 8b 00 63 00 f5 f5 f5 f5 ", // Quality score 99
                      val generateTemplateResponse: String = "fa fa fa fa 0c 00 8c 00 f5 f5 f5 f5 ", // OK response
                      val getTemplateFragmentsResponses: List<String>) {

    PERSON_1_VERSION_1_LEFT_THUMB_GOOD_SCAN(
            getTemplateFragmentsResponses = listOf(
                    "fa fa fa fa 75 00 96 00 00 00 64 00 00 46 4d 52 00 20 32 30 00 00 00 01 0e 00 00 01 2c 01 90 00 c5 00 c5 01 00 00 10 63 28 40 9c 00 2f 15 00 40 d1 00 3c 0d 00 40 ab 00 41 0d 00 40 24 00 52 1b 00 40 45 00 57 15 00 40 ce 00 5c 06 00 40 6c 00 5c 94 00 40 7d 00 61 0d 00 40 57 00 67 14 00 40 da 00 71 fb 00 40 ed 00 8c f8 00 80 6e 00 8f 0b 00 f5 f5 f5 f5 ",
                    "fa fa fa fa 75 00 96 00 01 00 64 00 00 40 23 00 94 14 00 40 ae 00 b5 83 00 80 7c 00 b8 09 00 40 bd 00 ce fe 00 40 f1 00 d6 75 00 40 4f 00 e7 98 00 40 f2 00 f0 f1 00 40 d5 00 f2 ef 00 40 a8 00 fc fb 00 80 7e 01 1e 07 00 40 a0 01 24 f5 00 40 da 01 25 ea 00 40 ad 01 35 e7 00 40 86 01 44 fe 00 41 22 01 48 d7 00 40 4a 01 58 b8 00 40 1c 01 5d f5 f5 f5 f5 ",
                    "fa fa fa fa 75 00 96 00 02 00 46 00 01 b1 00 40 9a 01 5f c6 00 40 8a 01 61 b3 00 80 7e 01 64 b1 00 40 d4 01 69 ca 00 80 b1 01 69 c5 00 40 f3 01 70 c7 00 40 e5 01 71 c6 00 41 00 01 72 ca 00 40 7c 01 74 38 00 40 59 01 7a d6 00 80 71 01 7f 68 00 00 00 ea 00 40 ad 01 35 e7 00 40 86 01 44 fe 00 41 22 01 48 d7 00 40 4a 01 58 b8 00 40 1c 01 5d f5 f5 f5 f5 "
            )),

    PERSON_1_VERSION_1_LEFT_INDEX_GOOD_SCAN(
            getTemplateFragmentsResponses = listOf(
                    "fa fa fa fa 75 00 96 00 00 00 64 00 00 46 4d 52 00 20 32 30 00 00 00 01 2c 00 00 01 2c 01 90 00 c5 00 c5 01 00 00 10 63 2d 40 50 00 26 8a 00 80 6c 00 2d 10 00 40 9b 00 2f 03 00 40 5e 00 75 86 00 40 e0 00 81 75 00 40 ae 00 8e f5 00 80 dc 00 95 f2 00 40 b1 00 b1 e8 00 80 66 00 b5 0a 00 40 b1 00 c1 de 00 40 88 00 cc db 00 80 1a 00 cd b3 00 f5 f5 f5 f5 ",
                    "fa fa fa fa 75 00 96 00 01 00 64 00 00 40 61 00 d6 06 00 80 ea 00 db da 00 40 68 00 e5 b3 00 40 63 00 e5 ac 00 40 79 00 e7 bd 00 40 bc 00 ec c3 00 80 c6 00 f5 c6 00 40 67 00 f6 a7 00 40 af 00 f7 bd 00 40 4f 01 00 35 00 40 2c 01 05 c6 00 40 1d 01 0c c6 00 80 98 01 0f bb 00 80 e3 01 13 c6 00 40 4b 01 13 07 00 41 03 01 13 ca 00 80 89 01 16 f5 f5 f5 f5 ",
                    "fa fa fa fa 75 00 96 00 02 00 64 00 01 b9 00 40 5d 01 16 8d 00 80 7b 01 19 b3 00 40 71 01 1a a2 00 40 11 01 20 ca 00 80 63 01 2a 93 00 40 dd 01 2e c0 00 40 ed 01 31 47 00 40 47 01 36 03 00 40 fc 01 39 c7 00 80 a3 01 39 b5 00 40 91 01 3a 33 00 40 6b 01 3d 24 00 80 38 01 46 e8 00 40 57 01 56 07 00 80 79 01 56 9e 00 40 55 01 7a fb 00 00 00 f5 f5 f5 f5 "
            )),

    PERSON_1_VERSION_2_LEFT_THUMB_GOOD_SCAN(
            getTemplateFragmentsResponses = listOf(
                    "fa fa fa fa 75 00 96 00 00 00 64 00 00 46 4d 52 00 20 32 30 00 00 00 01 38 00 00 01 2c 01 90 00 c5 00 c5 01 00 00 10 63 2f 80 8d 00 1a 91 00 40 61 00 20 a1 00 40 51 00 27 a0 00 40 cb 00 27 14 00 40 75 00 32 1f 00 40 dd 00 3a 10 00 80 43 00 3d 29 00 40 59 00 53 1e 00 40 7d 00 55 1a 00 41 03 00 56 06 00 40 a7 00 57 96 00 40 b7 00 5d 11 00 f5 f5 f5 f5 ",
                    "fa fa fa fa 75 00 96 00 01 00 64 00 00 40 92 00 63 14 00 41 0f 00 6e 00 00 40 40 00 7d 1a 00 81 1c 00 86 fc 00 80 a9 00 8b 09 00 40 5d 00 90 17 00 80 b1 00 b4 09 00 40 e1 00 b5 80 00 80 df 00 c9 f6 00 40 32 00 d7 1b 00 40 7c 00 e0 98 00 80 f8 00 f3 ef 00 41 18 00 f5 f1 00 40 c8 00 ff fb 00 40 1c 01 1e 9e 00 80 99 01 20 0a 00 40 b9 01 28 f5 f5 f5 f5 ",
                    "fa fa fa fa 75 00 96 00 02 00 64 00 00 f5 00 40 f7 01 2a ea 00 40 c5 01 3a e8 00 40 13 01 3b a4 00 40 2b 01 46 ae 00 40 99 01 47 fe 00 40 64 01 51 b5 00 40 3f 01 57 b5 00 80 9a 01 5a da 00 40 ac 01 61 c7 00 40 ed 01 6b cd 00 40 2a 01 70 35 00 41 00 01 73 ca 00 41 0f 01 73 d0 00 40 8c 01 74 50 00 40 70 01 76 d6 00 40 98 01 78 90 00 80 88 f5 f5 f5 f5 ",
                    "fa fa fa fa 75 00 96 00 03 00 0c 00 01 01 7d 6b 00 40 d4 01 80 c3 00 00 00 e8 00 40 13 01 3b a4 00 40 2b 01 46 ae 00 40 99 01 47 fe 00 40 64 01 51 b5 00 40 3f 01 57 b5 00 80 9a 01 5a da 00 40 ac 01 61 c7 00 40 ed 01 6b cd 00 40 2a 01 70 35 00 41 00 01 73 ca 00 41 0f 01 73 d0 00 40 8c 01 74 50 00 40 70 01 76 d6 00 40 98 01 78 90 00 80 88 f5 f5 f5 f5 "
            )),

    PERSON_1_VERSION_2_LEFT_INDEX_GOOD_SCAN(
            getTemplateFragmentsResponses = listOf(
                    "fa fa fa fa 75 00 96 00 00 00 64 00 00 46 4d 52 00 20 32 30 00 00 00 01 1a 00 00 01 2c 01 90 00 c5 00 c5 01 00 00 10 63 2a 40 9a 00 35 03 00 40 e9 00 7c 78 00 40 62 00 82 90 00 40 be 00 8d f8 00 40 f4 00 8d f4 00 40 c7 00 b1 ee 00 40 78 00 b2 10 00 40 82 00 b4 06 00 40 cd 00 c2 e1 00 40 a1 00 cd e3 00 40 7e 00 d7 06 00 41 12 00 d8 da 00 f5 f5 f5 f5 ",
                    "fa fa fa fa 75 00 96 00 01 00 64 00 00 80 8b 00 e1 bf 00 80 32 00 e1 b5 00 40 96 00 e6 c3 00 40 80 00 e6 b3 00 40 dd 00 ec ca 00 40 87 00 f7 a7 00 40 d0 00 f8 c0 00 40 f4 01 01 c6 00 40 6f 01 02 38 00 80 6d 01 0b ee 00 40 4d 01 0d cd 00 80 81 01 10 98 00 41 12 01 13 c7 00 40 be 01 16 bd 00 40 40 01 17 ca 00 80 9d 01 19 b9 00 80 ab 01 1b f5 f5 f5 f5 ",
                    "fa fa fa fa 75 00 96 00 02 00 52 00 01 bf 00 40 93 01 1c a7 00 41 08 01 28 c3 00 41 17 01 29 4a 00 80 88 01 2b 97 00 40 37 01 2c d0 00 40 b9 01 36 35 00 80 cd 01 37 b8 00 40 6f 01 38 06 00 40 92 01 3c 26 00 80 61 01 49 ef 00 40 85 01 54 0a 00 80 a2 01 55 9b 00 40 82 01 75 00 00 00 00 bd 00 40 40 01 17 ca 00 80 9d 01 19 b9 00 80 ab 01 1b f5 f5 f5 f5 "
            )),

    PERSON_2_VERSION_1_LEFT_THUMB_GOOD_SCAN(
            getTemplateFragmentsResponses = listOf(
                    "fa fa fa fa 75 00 96 00 00 00 64 00 00 46 4d 52 00 20 32 30 00 00 00 01 a4 00 00 01 2c 01 90 00 c5 00 c5 01 00 00 10 5f 41 40 67 00 2a 0d 00 41 0e 00 39 69 00 40 96 00 55 86 00 40 77 00 57 8a 00 80 65 00 5a 0d 00 41 01 00 5e e8 00 40 91 00 61 03 00 80 45 00 69 96 00 80 6a 00 6f 0b 00 40 d6 00 74 e3 00 80 b0 00 80 ef 00 80 3e 00 80 9b 00 f5 f5 f5 f5 ",
                    "fa fa fa fa 75 00 96 00 01 00 64 00 00 80 a6 00 95 e3 00 80 54 00 95 a4 00 40 ff 00 96 de 00 40 bc 00 97 d9 00 40 de 00 99 da 00 80 9b 00 9a 72 00 40 3a 00 9b a1 00 81 0f 00 9c 5e 00 40 cb 00 a3 d6 00 40 a4 00 a7 d0 00 40 39 00 ac ac 00 40 8e 00 b1 9e 00 40 93 00 b2 aa 00 40 0d 00 bf a8 00 40 70 00 bf 33 00 80 54 00 c3 bd 00 80 ac 00 c5 f5 f5 f5 f5 ",
                    "fa fa fa fa 75 00 96 00 02 00 64 00 00 b9 00 40 1c 00 cb b1 00 40 eb 00 cd d0 00 40 c6 00 cf c3 00 80 7f 00 e2 87 00 80 11 00 e6 b6 00 41 11 00 e9 d0 00 80 f1 00 ee c9 00 40 62 00 f1 f8 00 40 36 00 f1 cd 00 80 e0 00 f1 bf 00 40 ed 01 0f bb 00 40 65 01 12 75 00 40 f8 01 17 bd 00 40 4b 01 1b f4 00 40 ce 01 27 a0 00 80 ff 01 29 c2 00 40 c4 f5 f5 f5 f5 ",
                    "fa fa fa fa 75 00 96 00 03 00 64 00 00 01 2a 22 00 40 85 01 37 86 00 80 c9 01 3a 1e 00 40 df 01 3e 22 00 80 a6 01 44 90 00 80 84 01 48 09 00 40 6e 01 4a 03 00 41 1b 01 4d 5b 00 80 b1 01 54 0e 00 41 04 01 56 80 00 40 f0 01 56 06 00 40 b1 01 5c 0a 00 40 8a 01 60 06 00 41 18 01 64 71 00 40 9c 01 6b 00 00 40 fb 01 71 fe 00 41 17 01 74 78 00 f5 f5 f5 f5 ",
                    "fa fa fa fa 75 00 96 00 04 00 14 00 01 40 8f 01 78 f8 00 40 ba 01 79 00 00 80 d1 01 83 fc 00 00 00 22 00 80 a6 01 44 90 00 80 84 01 48 09 00 40 6e 01 4a 03 00 41 1b 01 4d 5b 00 80 b1 01 54 0e 00 41 04 01 56 80 00 40 f0 01 56 06 00 40 b1 01 5c 0a 00 40 8a 01 60 06 00 41 18 01 64 71 00 40 9c 01 6b 00 00 40 fb 01 71 fe 00 41 17 01 74 78 00 f5 f5 f5 f5 "
            )),

    PERSON_2_VERSION_1_LEFT_INDEX_GOOD_SCAN(
            getTemplateFragmentsResponses = listOf(
                    "fa fa fa fa 75 00 96 00 00 00 64 00 00 46 4d 52 00 20 32 30 00 00 00 01 26 00 00 01 2c 01 90 00 c5 00 c5 01 00 00 10 5e 2c 40 50 00 1e 0d 00 80 8c 00 25 f5 00 40 45 00 28 0a 00 80 66 00 39 84 00 40 94 00 3e e7 00 40 74 00 46 f8 00 80 a2 00 49 68 00 80 4a 00 56 9a 00 40 a5 00 69 de 00 40 95 00 78 da 00 80 52 00 7d 17 00 80 5a 00 84 aa 00 f5 f5 f5 f5 ",
                    "fa fa fa fa 75 00 96 00 01 00 64 00 00 80 86 00 84 d1 00 80 19 00 89 b2 00 80 61 00 8b b3 00 40 66 00 8b c3 00 40 6d 00 92 c3 00 80 24 00 95 bb 00 80 ad 00 9e cf 00 80 57 00 a3 3d 00 80 7d 00 b1 b2 00 80 91 00 b6 bf 00 80 87 00 b9 b3 00 80 c5 00 bb d0 00 80 a4 00 bd c3 00 80 73 00 be 35 00 80 68 00 c1 4c 00 80 52 00 c6 d6 00 40 75 00 d3 f5 f5 f5 f5 ",
                    "fa fa fa fa 75 00 96 00 02 00 5e 00 01 25 00 40 a9 00 df c6 00 40 11 00 e3 c7 00 80 a1 00 e7 b9 00 40 c0 00 ed d0 00 80 db 01 02 5f 00 80 9b 01 04 1c 00 40 77 01 0f 06 00 80 b4 01 10 0d 00 80 da 01 11 6c 00 80 53 01 14 e7 00 40 c4 01 1e 83 00 40 60 01 21 61 00 40 51 01 31 6e 00 40 e4 01 34 7b 00 40 a8 01 38 06 00 00 00 d6 00 40 75 00 d3 f5 f5 f5 f5 "
            )),

    PERSON_2_VERSION_2_LEFT_THUMB_GOOD_SCAN(
            getTemplateFragmentsResponses = listOf(
                    "fa fa fa fa 75 00 96 00 00 00 64 00 00 46 4d 52 00 20 32 30 00 00 00 01 86 00 00 01 2c 01 90 00 c5 00 c5 01 00 00 10 63 3c 40 9e 00 1e 0d 00 40 e8 00 22 75 00 40 f1 00 2a 78 00 40 fb 00 32 7b 00 80 c4 00 39 89 00 40 cf 00 3e 00 00 40 bb 00 7a 8d 00 80 b0 00 87 0d 00 80 fc 00 8a ef 00 80 75 00 94 91 00 80 97 00 9d 10 00 80 de 00 a4 f5 00 f5 f5 f5 f5 ",
                    "fa fa fa fa 75 00 96 00 01 00 64 00 00 80 72 00 ac 9a 00 81 0d 00 af e3 00 40 ed 00 b9 e0 00 80 d8 00 c0 eb 00 40 ff 00 c4 dd 00 80 8a 00 c5 a7 00 40 71 00 c7 a2 00 80 d1 00 ca 7c 00 80 29 00 cc a0 00 80 32 00 d2 1f 00 40 db 00 d5 d0 00 40 71 00 d9 aa 00 41 23 00 e9 cd 00 40 45 00 e9 a4 00 40 20 00 eb a1 00 80 e3 00 ee c0 00 40 fe 00 f4 f5 f5 f5 f5 ",
                    "fa fa fa fa 75 00 96 00 02 00 64 00 00 c3 00 40 54 00 f5 ae 00 40 a7 00 f5 30 00 80 8b 00 f7 c2 00 40 ac 00 f8 28 00 40 26 01 0c ac 00 40 45 01 11 b5 00 81 16 01 12 c0 00 80 99 01 1b e8 00 40 af 01 1c 83 00 40 70 01 1e ca 00 80 f4 01 25 ae 00 40 27 01 26 ac 00 40 32 01 28 c3 00 41 1b 01 36 b5 00 41 25 01 3d b8 00 80 cf 01 3e 98 00 40 48 f5 f5 f5 f5 ",
                    "fa fa fa fa 75 00 96 00 03 00 5a 00 01 01 41 50 00 80 ab 01 41 80 00 80 82 01 44 e4 00 40 3d 01 46 58 00 81 07 01 50 a8 00 40 fa 01 53 25 00 40 75 01 62 67 00 40 b8 01 67 87 00 80 4a 01 6a 6b 00 41 12 01 6a 26 00 40 29 01 70 78 00 40 cf 01 75 8a 00 80 b9 01 77 09 00 80 9a 01 7d ff 00 40 f3 01 82 17 00 00 00 b8 00 80 cf 01 3e 98 00 40 48 f5 f5 f5 f5 "
            )),

    PERSON_2_VERSION_2_LEFT_INDEX_GOOD_SCAN(
            getTemplateFragmentsResponses = listOf(
                    "fa fa fa fa 75 00 96 00 00 00 64 00 00 46 4d 52 00 20 32 30 00 00 00 01 32 00 00 01 2c 01 90 00 c5 00 c5 01 00 00 10 63 2e 40 ea 00 29 de 00 80 87 00 31 86 00 80 c3 00 3d 6b 00 40 99 00 3d f8 00 80 72 00 4f 98 00 80 c9 00 5c d9 00 40 b9 00 70 d0 00 80 7b 00 76 1a 00 80 3f 00 7a af 00 80 83 00 7c a5 00 80 ab 00 7e ca 00 40 8e 00 83 bb 00 f5 f5 f5 f5 ",
                    "fa fa fa fa 75 00 96 00 01 00 64 00 00 80 88 00 84 b1 00 40 93 00 8a bd 00 80 4a 00 8a b1 00 40 80 00 94 30 00 40 cb 00 97 c7 00 80 8b 00 a9 38 00 40 84 00 a9 40 00 40 dd 00 b5 cd 00 80 aa 00 b6 b9 00 40 9c 00 ba aa 00 80 6f 00 bc d0 00 40 b7 00 bc c0 00 40 8b 00 bd 2f 00 40 8e 00 cc 21 00 40 25 00 d3 c0 00 40 bb 00 d9 bd 00 80 b2 00 e5 f5 f5 f5 f5 ",
                    "fa fa fa fa 75 00 96 00 02 00 64 00 00 b5 00 40 cf 00 e8 ca 00 80 e4 00 fb 55 00 80 aa 01 00 1e 00 40 88 01 08 03 00 80 66 01 08 e0 00 80 e6 01 0b 6e 00 40 2c 01 0c 58 00 80 d4 01 14 7b 00 40 40 01 19 5d 00 41 06 01 1d 6c 00 80 64 01 21 6b 00 40 3c 01 27 6c 00 40 eb 01 2d 80 00 80 b1 01 3a 09 00 80 64 01 6b 76 00 80 ca 01 6f fc 00 80 77 f5 f5 f5 f5 ",
                    "fa fa fa fa 75 00 96 00 03 00 06 00 01 01 70 f2 00 00 00 ca 00 80 e4 00 fb 55 00 80 aa 01 00 1e 00 40 88 01 08 03 00 80 66 01 08 e0 00 80 e6 01 0b 6e 00 40 2c 01 0c 58 00 80 d4 01 14 7b 00 40 40 01 19 5d 00 41 06 01 1d 6c 00 80 64 01 21 6b 00 40 3c 01 27 6c 00 40 eb 01 2d 80 00 80 b1 01 3a 09 00 80 64 01 6b 76 00 80 ca 01 6f fc 00 80 77 f5 f5 f5 f5"
            )),

    PERSON_1_VERSION_1_LEFT_THUMB_BAD_SCAN(
            imageQualityResponse = "fa fa fa fa 0e 00 8b 00 24 00 f5 f5 f5 f5 ", // Quality score 36
            getTemplateFragmentsResponses = listOf(
                    "fa fa fa fa 75 00 96 00 00 00 48 00 01 46 4d 52 00 20 32 30 00 00 00 00 48 00 00 01 2c 01 90 00 c5 00 c5 01 00 00 10 26 07 80 ea 00 1a f8 00 81 05 00 1b f6 00 81 14 00 44 dd 00 40 b9 01 34 96 00 80 6d 01 38 11 00 40 ac 01 4e 14 00 40 b9 01 62 0d 00 00 00 d4 00 40 15 01 72 d0 00 40 2f 01 73 d0 00 40 4a 01 78 d6 00 40 93 01 7e de 00 80 ac f5 f5 f5 f5 "
            )),

    PERSON_1_VERSION_1_LEFT_INDEX_BAD_SCAN(
            imageQualityResponse = "fa fa fa fa 0e 00 8b 00 24 00 f5 f5 f5 f5 ", // Quality score 36
            getTemplateFragmentsResponses = listOf(
                    "fa fa fa fa 75 00 96 00 00 00 64 00 00 46 4d 52 00 20 32 30 00 00 00 00 78 00 00 01 2c 01 90 00 c5 00 c5 01 00 00 10 36 0f 40 9a 00 b3 8a 00 80 88 00 b9 03 00 80 93 00 c6 86 00 40 d3 00 d0 78 00 40 b1 00 d3 00 00 80 c1 00 dc 82 00 40 b0 00 e8 03 00 80 cb 00 f2 7f 00 40 7f 00 f5 0a 00 80 e9 00 fe 7f 00 40 dc 01 0a 03 00 40 b4 01 10 83 00 f5 f5 f5 f5 ",
                    "fa fa fa fa 75 00 96 00 01 00 14 00 01 40 6f 01 11 07 00 40 d1 01 41 87 00 40 df 01 4d 03 00 00 00 00 c5 01 00 00 10 36 0f 40 9a 00 b3 8a 00 80 88 00 b9 03 00 80 93 00 c6 86 00 40 d3 00 d0 78 00 40 b1 00 d3 00 00 80 c1 00 dc 82 00 40 b0 00 e8 03 00 80 cb 00 f2 7f 00 40 7f 00 f5 0a 00 80 e9 00 fe 7f 00 40 dc 01 0a 03 00 40 b4 01 10 83 00 f5 f5 f5 f5 "
            )),

    NO_FINGER(
            generateTemplateResponse = "fa fa fa fa 0e 00 8c 0d 69 00 f5 f5 f5 f5 ", // SDK error
            getTemplateFragmentsResponses = listOf());

    companion object {

        val person1TwoFingersGoodScan =
                arrayOf(PERSON_1_VERSION_1_LEFT_THUMB_GOOD_SCAN,
                        PERSON_1_VERSION_1_LEFT_INDEX_GOOD_SCAN)

        val person1TwoFingersAgainGoodScan =
                arrayOf(PERSON_1_VERSION_2_LEFT_THUMB_GOOD_SCAN,
                        PERSON_1_VERSION_2_LEFT_INDEX_GOOD_SCAN)

        val person2TwoFingersGoodScan =
                arrayOf(PERSON_2_VERSION_1_LEFT_THUMB_GOOD_SCAN,
                        PERSON_2_VERSION_1_LEFT_INDEX_GOOD_SCAN)

        val person2TwoFingersAgainGoodScan =
                arrayOf(PERSON_2_VERSION_2_LEFT_THUMB_GOOD_SCAN,
                        PERSON_2_VERSION_2_LEFT_INDEX_GOOD_SCAN)
    }
}
