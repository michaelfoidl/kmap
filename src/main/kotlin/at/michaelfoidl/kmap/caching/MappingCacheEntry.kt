/*
 * kmap
 * version 0.1.2
 *
 * Copyright (c) 2018, Michael Foidl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.michaelfoidl.kmap.caching

import at.michaelfoidl.kmap.initializable.Initializable


/**
 * Represents a single entry in the cache consisting of the source object, the name of the class of the target object
 * and the target object itself which is the result of the mapping process.
 *
 * @since 0.1
 * @constructor Creates a new entry with the given values that can then be added to the cache.
 * @property source The source object.
 * @property targetClassName The name of the class of the target object.
 * @property target The mapped target object.
 */
internal class MappingCacheEntry<SourceT : Any?, TargetT : Any?>(
        var source: SourceT,
        var targetClassName: String,
        var target: Initializable<TargetT?>
)