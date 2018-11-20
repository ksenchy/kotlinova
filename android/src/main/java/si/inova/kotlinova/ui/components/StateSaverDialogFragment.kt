package si.inova.kotlinova.ui.components

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.fragment.app.DialogFragment
import si.inova.kotlinova.ui.state.StateSaverManager
import si.inova.kotlinova.ui.state.StateSavingComponent

/**
 * Dialog fragment that provides automatic state saving facilities out of the box
 *
 * @author Matej Drobnic
 */
open class StateSaverDialogFragment : DialogFragment(), StateSavingComponent {
    override val stateSaverManager = StateSaverManager()
    /**
     * Whether this dialog fragment has just been created for the first time.
     *
     * *true* means dialog fragment has been created for the first time.
     *
     * *false* means dialog fragment has been recreated from the previous state
     * (for example when being recreated from configuration change or from backstack)
     */
    var createdForTheFirstTime = true
        private set

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            stateSaverManager.restoreInstance(savedInstanceState)
        }

        super.onCreate(savedInstanceState)

        createdForTheFirstTime = createdForTheFirstTime && savedInstanceState == null
    }

    @CallSuper
    override fun onSaveInstanceState(outState: Bundle) {
        stateSaverManager.saveInstance(outState)
        super.onSaveInstanceState(outState)
    }

    @CallSuper
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        createdForTheFirstTime = false
    }
}