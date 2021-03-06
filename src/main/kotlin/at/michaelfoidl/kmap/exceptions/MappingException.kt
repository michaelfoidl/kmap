/*
 * kmap
 * version 0.2
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

package at.michaelfoidl.kmap.exceptions


/**
 * An error that can occur during the mapping process. The [message] or the underlying [cause] might provide more detailed
 * information.
 *
 * @since 0.1
 * @constructor Creates a new [Exception].
 * @param message the message describing the error that has occurred.
 * @param cause the cause that has lead to this error.
 */
class MappingException(message: String? = null, cause: Throwable? = null) : kotlin.Exception(message, cause)