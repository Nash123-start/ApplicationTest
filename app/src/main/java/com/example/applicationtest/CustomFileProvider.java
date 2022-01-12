/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.example.applicationtest;

import androidx.core.content.FileProvider;

/**
 * This FileProvider allows the app to export files to other apps.
 *
 * Will automatically be blocked by MAM if necessary.
 */
public class CustomFileProvider extends FileProvider { }
