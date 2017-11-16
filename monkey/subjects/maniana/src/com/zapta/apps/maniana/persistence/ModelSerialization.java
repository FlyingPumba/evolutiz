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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.zapta.apps.maniana.annotations.ApplicationScope;
import com.zapta.apps.maniana.model.AppModel;
import com.zapta.apps.maniana.model.ItemColor;
import com.zapta.apps.maniana.model.ItemModelReadOnly;
import com.zapta.apps.maniana.model.PageKind;

/**
 * Serializes a model to a JSON doc.
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public class ModelSerialization implements FieldNames {

    // Format Step:
    // 1: initial. Model fields at top level, with format.
    // 2: added top level "metadata". Move model fields to "model". Format field stays at
    // top level.
    private static final int FORMAT_STEP = 2;

    public static final String serializeModel(AppModel model, PersistenceMetadata metadata) {
        try {
            final JSONObject root = new JSONObject();
            root.put(FIELD_FORMAT, FORMAT_STEP);
            root.put(FIELD_METADATA, metadata.toJason());
            root.put(FIELD_MODEL, modelToJason(model));

            // NOTE: using indent of only 1 to reduce file size.
            return root.toString(1);
        } catch (JSONException e) {
            throw new RuntimeException("JSON serialization error", e);
        }
    }

    /** Serialize a model to a JSON object. */
    private static final JSONObject modelToJason(AppModel model) throws JSONException {
        final JSONObject root = new JSONObject();
        root.put(FIELD_LAST_PUSH_DATE, model.getLastPushDateStamp());
        root.put(FIELD_TODAY, pageItemsToJson(model, PageKind.TODAY));
        root.put(FIELD_TOMOROW, pageItemsToJson(model, PageKind.TOMOROW));
        return root;
    }

    /** Serialize one page */
    private static final JSONArray pageItemsToJson(AppModel appModel, PageKind pageKind)
            throws JSONException {

        final JSONArray result = new JSONArray();
        final int n = appModel.getPageItemCount(pageKind);
        for (int i = 0; i < n; i++) {
            result.put(itemToJson(appModel.getItemReadOnly(pageKind, i)));
        }
        return result;
    }

    /** Serialzie one item */
    private static final JSONObject itemToJson(ItemModelReadOnly itemModel) throws JSONException {
        final JSONObject result = new JSONObject();
        result.put(FIELD_UPDATE_TIME, itemModel.getUpdateTime());
        result.put(FIELD_ID, itemModel.getId());
        result.put(FIELD_TEXT, itemModel.getText());
        if (itemModel.isCompleted()) {
            result.put(FIELD_DONE, itemModel.isCompleted());
        }
        if (itemModel.isLocked()) {
            result.put(FIELD_LOCKED, itemModel.isLocked());
        }
        if (itemModel.getColor() != ItemColor.NONE) {
            result.put(FIELD_COLOR, itemModel.getColor().getKey());
        }
        return result;
    }

}
