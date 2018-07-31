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
import com.zapta.apps.maniana.model.ItemModel;
import com.zapta.apps.maniana.model.PageKind;
import com.zapta.apps.maniana.util.IdGenerator;
import com.zapta.apps.maniana.util.LogUtil;

/**
 * Performs model deserialization from JSON doc.
 * 
 * TODO: define consts for the json field names.
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public class ModelDeserialization implements FieldNames {

    /**
     * Deserialize a model from a JSON doc.
     * 
     * @param resultModel filled in with the deserialize model data. All previous content is lost,
     *        regardless of the outcome.
     * 
     * @param resultMetadata filled in with the deserialized metadata. All previous content is lost,
     *        regardless of the outcome.
     * 
     * @throws JSONException
     */
    public static final void deserializeModel(AppModel resultModel,
            PersistenceMetadata resultMetadata, String jsonString) throws JSONException {
        resultModel.clear();
        resultMetadata.clear();

        final JSONObject root = new JSONObject(jsonString);

        final int format = root.getInt(FIELD_FORMAT);
        if (format < 2) {
            LogUtil.info("Loading data file in old format: " + format);
            // In format version 1 the model fields were at top level and we did not have metadata.
            populateModelFields(root, resultModel);
            // NOTE: metadata was already cleared above.
        } else {
            // For format >= 2.
            populateModelFields(root.getJSONObject(FIELD_MODEL), resultModel);
            resultMetadata.fromJson(root.getJSONObject(FIELD_METADATA));
        }
    }

    /**
     * Deserialize the model fields. Note that in format version 1 the model fields were at top json
     * level and in format >= 2, the model fields are in a 'model' sub field.
     */
    private static final void populateModelFields(JSONObject root, AppModel appModel)
            throws JSONException {
        appModel.setLastPushDateStamp(root.optString(FIELD_LAST_PUSH_DATE, ""));
        populateItemListFromJason(root.getJSONArray(FIELD_TODAY), appModel, PageKind.TODAY);
        populateItemListFromJason(root.getJSONArray(FIELD_TOMOROW), appModel, PageKind.TOMOROW);
    }

    /** Deserialize a page item list */
    private static final void populateItemListFromJason(JSONArray jsonItems, AppModel appModel,
            PageKind pageKind) throws JSONException {
        for (int i = 0; i < jsonItems.length(); i++) {
            final ItemModel item = modelItemFromJson(jsonItems.getJSONObject(i));
            // Force item in the today page to be unlocked.
            if (pageKind.isToday() && item.isLocked()) {
                LogUtil.warning("Cleared lock state while loading a today item");
                item.setIsLocked(false);
            }
            appModel.appendItem(pageKind, modelItemFromJson(jsonItems.getJSONObject(i)));
        }
    }

    /** Deserialize a single item */
    private static final ItemModel modelItemFromJson(JSONObject jsonItem) throws JSONException {
        final String optId = jsonItem.optString(FIELD_ID, null);
        final String id = (optId == null) ? IdGenerator.getFreshId() : optId;

        final long optUpdateTime = jsonItem.optLong(FIELD_UPDATE_TIME);
        final long updateTime = (optUpdateTime == 0) ? System.currentTimeMillis() : optUpdateTime;

        final String text = jsonItem.getString(FIELD_TEXT);
        final boolean isCompleted = jsonItem.optBoolean(FIELD_DONE);
        final boolean isLocked = jsonItem.optBoolean(FIELD_LOCKED);

        final String optColorKey = jsonItem.optString(FIELD_COLOR, null);
        // NOTE(tal): NONE may or may not be in the current user selected task color set.
        // Default to NONE if not found.
        final ItemColor color = (optColorKey == null) ? ItemColor.NONE : ItemColor.fromKey(
                optColorKey, ItemColor.NONE);

        return new ItemModel(updateTime, id, text, isCompleted, isLocked, color);
    }

}
