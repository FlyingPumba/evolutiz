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

package com.zapta.apps.maniana.model;

import com.zapta.apps.maniana.annotations.ApplicationScope;

/**
 * What items should be moved from Tomorow to Today
 * 
 * @author Tal Dayan
 */
@ApplicationScope
public enum PushScope {
    NONE,
    UNLOCKED_ONLY,
    ALL;
    
  public final boolean isActive() {
      return this != NONE;
  }
}
