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

import android.content.Context;

import com.zapta.apps.maniana.annotations.MainActivityScope;
import com.zapta.apps.maniana.main.MainActivityState;
import com.zapta.apps.maniana.model.AppModel;
import com.zapta.apps.maniana.persistence.ModelReadingResult.ModelLoadingOutcome;
import com.zapta.apps.maniana.util.FileUtil;
import com.zapta.apps.maniana.util.FileUtil.FileReadResult;
import com.zapta.apps.maniana.util.FileUtil.FileReadResult.FileReadOutcome;
import com.zapta.apps.maniana.util.LogUtil;

/**
 * Manages model persistence.
 * 
 * @author Tal Dayan
 */
// TODO: add syncrhonization and make it an app scope level.
@MainActivityScope
public class ModelPersistence {

    /** Path to file where model is persisted. */
    public static final String DATA_FILE_NAME = "maniana_data.json";

    /** Static lock protecting the access to the data file. */
    public static final Object sDataFileLock = new Object();

    /** Read the model file from the internal storage. */
    public static final ModelReadingResult readModelFile(Context context, AppModel resultModel) {
        ModelReadingResult result = readModelFileInternal(context, resultModel, DATA_FILE_NAME,
                false);
        if (result.outcome.isOk()) {
            // Model is same as persistence file and no version change.
            resultModel.setClean();
        } else {
            // Model need to be rewritten
            resultModel.setDirty();
        }
        return result;
    }

    /**
     * Caller is expected to manager the model's dirty bit. In case of an error, the returned model
     * is cleared.
     */
    private static final ModelReadingResult readModelFileInternal(Context context,
            AppModel resultModel, String fileName, boolean isAsset) {
        LogUtil.info("Going to read data from " + (isAsset ? "assert" : "data File") + " "
                + fileName);

        resultModel.clear();

        // Try to read the model file
        final FileReadResult fileReadResult;
        synchronized (sDataFileLock) {
            fileReadResult = FileUtil.readFileToString(context, fileName, isAsset);
        }

        if (fileReadResult.outcome == FileReadOutcome.NOT_FOUND) {
            return new ModelReadingResult(ModelLoadingOutcome.FILE_NOT_FOUND);
        }

        // Try to parse the json file
        try {
            PersistenceMetadata resultMetadata = new PersistenceMetadata();
            ModelDeserialization.deserializeModel(resultModel, resultMetadata,
                    fileReadResult.content);
            return new ModelReadingResult(ModelLoadingOutcome.FILE_READ_OK, resultMetadata);

        } catch (JSONException e) {
            LogUtil.error(e, "Error parsing model JSON");
            resultModel.clear();
            return new ModelReadingResult(ModelLoadingOutcome.FILE_HAS_ERRORS);
        }
    }

    public static final void writeModelFile(MainActivityState mainActivityState, AppModel model,
            PersistenceMetadata metadata) {
        LogUtil.info("Saving model to file: " + DATA_FILE_NAME);
        final String json = ModelSerialization.serializeModel(model, metadata);
        synchronized (sDataFileLock) {
            FileUtil.writeStringToFile(mainActivityState.context(), json, DATA_FILE_NAME, Context.MODE_PRIVATE);
        }
        // Model reflects persisted state.
        model.setClean();
    }
}
