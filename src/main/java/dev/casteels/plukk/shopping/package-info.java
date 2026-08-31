@org.springframework.modulith.ApplicationModule(
        displayName = "Shopping",
        allowedDependencies = {"catalog", "household", "identity", "shared::notification"}
)
package dev.casteels.plukk.shopping;
