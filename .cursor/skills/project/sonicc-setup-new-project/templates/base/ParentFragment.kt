package YOUR.PACKAGE.core.ui.base.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * Template — copy to `:core-ui` …/base/fragment/
 * Replace YOUR.PACKAGE with applicationId root.
 *
 * Hooks (reference: QiblaCompass BaseFragment):
 *  - onViewInflated()           — optional, right after binding inflate (before the view is attached)
 *  - initObservers()            — optional, collectors on viewLifecycleOwner
 *  - onViewCreated(Bundle?)     — optional, when savedInstanceState is needed
 *  - onViewCreated()            — mandatory screen setup
 */
abstract class ParentFragment<T : ViewBinding>(
    private val bindingFactory: (LayoutInflater) -> T,
) : Fragment() {

    private var _binding: T? = null
    protected val binding: T
        get() = _binding ?: error("Binding accessed outside of view lifecycle.")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = bindingFactory(inflater)
        onViewInflated()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        onViewCreated(savedInstanceState)
        onViewCreated()
    }

    open fun onViewInflated() {}

    open fun initObservers() {}

    open fun onViewCreated(savedInstanceState: Bundle?) {}

    abstract fun onViewCreated()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
