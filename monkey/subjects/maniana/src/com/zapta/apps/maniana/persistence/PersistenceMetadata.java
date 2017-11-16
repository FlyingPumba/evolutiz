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

package com.zapta.apps.maniana.persistence;

import org.json.JSONException;
import org.json.JSONObject;

import com.zapta.apps.maniana.annotations.ApplicationScope;

/** Represents additional data that is persisted with the model. */
@ApplicationScope
public class PersistenceMetadata {

    public static final int DEFAULT_WRITER_VERSION_CODE = 0;
    public static final String DEFAULT_WRITER_VERSION_NAME = "";

    /** Build number of the app that wrote the model. Same as the field of same in the app manifest */
    public int writerVersionCode;

    /** Build name of the app that wrote the model. Same as the field of same in the app manifest */
    public String writerVersionName;

    public PersistenceMetadata() {
        clear();
    }

    public final void clear() {
        writerVersionCode = DEFAULT_WRITER_VERSION_CODE;
        writerVersionName = DEFAULT_WRITER_VERSION_NAME;
    }

    public PersistenceMetadata(int writerVersionCode, String writerVersionName) {
        super();
        this.writerVersionCode = writerVersionCode;
        this.writerVersionName = writerVersionName;
    }

    public JSONObject toJason() throws JSONException {
        final JSONObject result = new JSONObject();
        // TODO: define consts for the field names
        result.put("writer_ver_code", writerVersionCode);
        result.put("writer_ver_name", writerVersionName);
        return result;
    }

    public void fromJson(JSONObject json) throws JSONException {
        clear();
        // These two fields are required but we provide default to avoid force close, just in case.
        writerVersionCode = json.optInt("writer_ver_code", DEFAULT_WRITER_VERSION_CODE);
        writerVersionName = json.optString("writer_ver_name", DEFAULT_WRITER_VERSION_NAME);
    }
}
