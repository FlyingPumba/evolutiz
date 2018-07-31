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

import static com.zapta.apps.maniana.util.Assertions.check;
import static com.zapta.apps.maniana.util.Assertions.checkNotNull;

import javax.annotation.Nullable;

import com.zapta.apps.maniana.annotations.ApplicationScope;

@ApplicationScope
public class ModelReadingResult {

    public static enum ModelLoadingOutcome {
        FILE_READ_OK,
        FILE_NOT_FOUND,
        FILE_HAS_ERRORS;

        public final boolean isOk() {
            return this == FILE_READ_OK;
        }
    }

    public final ModelLoadingOutcome outcome;

    /** Not null IFF status is READ OK */
    @Nullable
    public final PersistenceMetadata metadata;

    /** Constructor for OK outcome. */
    public ModelReadingResult(ModelLoadingOutcome outcome, PersistenceMetadata metadata) {
        check(outcome.isOk());
        this.outcome = outcome;
        this.metadata = checkNotNull(metadata);
    }

    /** Constructor for an error outcome. */
    public ModelReadingResult(ModelLoadingOutcome outcome) {
        check(!outcome.isOk());
        this.outcome = outcome;
        this.metadata = null;
    }
}
