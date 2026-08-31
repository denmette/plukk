@org.springframework.modulith.ApplicationModule(
        displayName = "Shopping",
        allowedDependencies = {"catalog::api", "household::api", "identity", "shared::notification"}
)
package dev.casteels.plukk.shopping;
