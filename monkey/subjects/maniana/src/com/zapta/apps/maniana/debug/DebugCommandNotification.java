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

package com.zapta.apps.maniana.debug;

import com.zapta.apps.maniana.annotations.MainActivityScope;

/**
 * Commands of debug notification dialog.
 *
 * @author: Tal Dayan
 */
@MainActivityScope
public enum DebugCommandNotification implements DebugCommand {
    NOTIFICATION_SINGLE("Notification (1)"),
    NOTIFICATION_MULTI("Notification (17)"),
    NOTIFICATION_DELAYED("Notification (in 10 secs)"),
    NOTIFICATION_CLEAR("Notification (clear)");
    
    private final String mText;
    
    private DebugCommandNotification(String text) {
        this.mText = text;
    }
    
    @Override
    public String getText() {
        return mText;
    }
}
