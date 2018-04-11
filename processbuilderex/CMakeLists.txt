cmake_minimum_required(VERSION 3.4.1)

add_library(Native SHARED src/main/cpp/native-lib.cpp)

find_library(log-lib log )


target_link_libraries(Native android ${log-lib})