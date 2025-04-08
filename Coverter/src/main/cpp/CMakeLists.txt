
# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

set(CMAKE_CXX_FLAGE "${CMAKE_CXX_FLAGS} -std=c++11 -Wall -Werror")
set(LIB_MP3 Mp3Codec)
set(LIB_LAME AndroidLame)
set(LIB_OPUS OpusCodec)
add_subdirectory(libmp3lame)

add_subdirectory(lameutils)
add_subdirectory(opus)

add_library(
        ${LIB_MP3}
        SHARED
        mp3codec.cpp
)

add_library(
        ${LIB_LAME}
        SHARED
        AndroidLame.cpp
)
add_library(
        ${LIB_OPUS}
        SHARED
        OpusConverter.cpp
)
find_library(
        log-lib
        log)

target_link_libraries(${LIB_MP3}
        ${log-lib}
        #mp3lame
        lame_utils)
target_link_libraries(${LIB_LAME}
        ${log-lib}
        #mp3lame
        libmp3lame)
target_link_libraries(${LIB_OPUS}
        ${log-lib}
        #mp3lame
        opus)
