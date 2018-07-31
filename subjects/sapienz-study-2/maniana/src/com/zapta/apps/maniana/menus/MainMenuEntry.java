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

package com.zapta.apps.maniana.menus;

import com.zapta.apps.maniana.R;
import com.zapta.apps.maniana.annotations.MainActivityScope;

/**
 * Main menu entries.
 * 
 * @author Tal Dayan
 */
@MainActivityScope
public enum MainMenuEntry {
    ABOUT(R.drawable.main_menu_about, R.string.main_menu_About),
    HELP(R.drawable.main_menu_help, R.string.main_menu_Help),
    SETTINGS(R.drawable.main_menu_settings, R.string.main_menu_Settings),
    DEBUG(R.drawable.main_menu_debug, R.string.main_menu_debug);

    public final int iconResourceId;
    public final int textResourceId;

    private MainMenuEntry(int iconResourceId, int textResourceId) {
        this.iconResourceId = iconResourceId;
        this.textResourceId = textResourceId;
    }
}