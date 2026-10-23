package YOUR.PACKAGE.core.ui.base.enums

/**
 * Template — copy to `:core-ui` …/base/enums/
 * Replace YOUR.PACKAGE with applicationId root.
 *
 * Edge-to-edge padding mode applied by ParentActivity on the root view.
 * Override `ParentActivity.getInsetsMode()` per Activity (StartupActivity / MainActivity).
 */
enum class InsetsMode {
    EDGE_TO_EDGE, // No padding — content draws under status + nav bars
    TOP,          // Status bar only
    BOTTOM,       // Nav bar / IME only
    TOP_BOTTOM,   // Both (default)
}
