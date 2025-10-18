cmake_minimum_required(VERSION 3.18.1)

project("dietplanner")

add_library(
        dietplanner-lib
        SHARED
        native-lib.c)

find_library(
        log-lib
        log)

target_link_libraries(
        dietplanner-lib
        ${log-lib})