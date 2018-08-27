/*
 * kmap
 *
 * Copyright (c) 2018, Michael Foidl.
 */

package at.michaelfoidl.kmap.caching

import at.michaelfoidl.kmap.initializable.Initializable


internal class MappingCacheEntry<SourceT: Any?, TargetT: Any?>(
        var source: SourceT,
        var targetClassName: String,
        var target: Initializable<TargetT?>
)