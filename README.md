---
title: Bluetooth Printer
description: Print via bluetooth
---
<!--
# license: Licensed to the Apache Software Foundation (ASF) under one
#         or more contributor license agreements.  See the NOTICE file
#         distributed with this work for additional information
#         regarding copyright ownership.  The ASF licenses this file
#         to you under the Apache License, Version 2.0 (the
#         "License"); you may not use this file except in compliance
#         with the License.  You may obtain a copy of the License at
#
#           http://www.apache.org/licenses/LICENSE-2.0
#
#         Unless required by applicable law or agreed to in writing,
#         software distributed under the License is distributed on an
#         "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#         KIND, either express or implied.  See the License for the
#         specific language governing permissions and limitations
#         under the License.
-->

# cordova-plugin-datamax

This plugin allow to print via bluetooth.

This plugin provides a way to connect with the paired bluetooth printer and get printed when click on "Print" Button.

This plugin defines global objects including `navigator.blePrinter`.

Although in the global scope, they are not available until after the `deviceready` event.

    document.addEventListener("deviceready", onDeviceReady, false);
    function onDeviceReady() {
        console.log(navigator.blePrinter);
    }

## Installation

    cordova plugin add cordova-plugin-datamax

## Supported Platforms
- Android

For printing
---------------------

navigator.blePrinter.print(String str, successcallback, errorcallback);

## Supported Platforms
- Android
- 
Quick Example
-------------------------
navigator.blePrinter.print(stringdata,
    function (results) {
        alert(JSON.stringify(results));
    },
            function (error) {
                alert(JSON.stringify(error));
            }
        );


