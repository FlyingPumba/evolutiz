/*
 * Copyright (C) 2011 The original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.zapta.apps.maniana.controller;

import com.zapta.apps.maniana.annotations.MainActivityScope;

/**
 * The app starts with these cases of model loading.
 * 
 * @author Tal Dayan
 */
@MainActivityScope
public enum MainActivityStartupKind {
    /** Loaded model data file, no app version change. */
    NORMAL,
    /** No model data file, loaded sample data. */
    NEW_USER,
    /** Loaded model data file. Current app version is different than file writer */
    NEW_VERSION_ANNOUNCE,
    /** Same as NEW_VERSION but should suppress new version message. */
    NEW_VERSION_SILENT,
    /** Model data file found but has error reading/parsing. */
    MODEL_DATA_ERROR;
}
