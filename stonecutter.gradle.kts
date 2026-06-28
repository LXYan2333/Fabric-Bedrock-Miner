plugins {
	id("dev.kikugie.stonecutter")
}

stonecutter active "26.2"

stonecutter parameters {
	swaps["mod_version"] = "\"${property("mod.version")}\";"
	swaps["minecraft"] = "\"${node.metadata.version}\";"

	replacements {
		string(current.parsed >= "26.1") {
			replace("classTweaker v2 named", "classTweaker v2 official")
		}
	}
}
