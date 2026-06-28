package com.example.aichatassisstant.presentation.ui.chat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aichatassisstant.R
import com.example.aichatassisstant.databinding.FragmentChatBinding
import com.example.aichatassisstant.presentation.viewmodel.ChatUiState
import com.example.aichatassisstant.presentation.viewmodel.ChatViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by viewModels()
    private val chatAdapter = ChatAdapter()
    private var apiKeyDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupWindowInsets()
        setupToolbarMenu()
        setupRecyclerView()
        setupInputBar()
        observeUiState()
    }

    override fun onDestroyView() {
        apiKeyDialog?.dismiss()
        apiKeyDialog = null
        super.onDestroyView()
        _binding = null
    }

    private fun setupToolbar() {
        (requireActivity() as androidx.appcompat.app.AppCompatActivity).setSupportActionBar(binding.toolbar)
    }

    private fun setupWindowInsets() {
        val inputMargin = resources.getDimensionPixelSize(R.dimen.input_margin)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val bottomInset = maxOf(systemBars.bottom, ime.bottom)

            binding.toolbar.setPadding(
                binding.toolbar.paddingLeft,
                systemBars.top,
                binding.toolbar.paddingRight,
                binding.toolbar.paddingBottom
            )

            binding.inputContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
                bottomMargin = inputMargin + bottomInset
            }

            insets
        }
    }

    private fun setupToolbarMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_chat, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_api_key -> {
                        viewModel.showApiKeyDialog()
                        true
                    }
                    R.id.action_clear_chat -> {
                        showClearChatConfirmation()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupRecyclerView() {
        binding.recyclerMessages.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            itemAnimator = null
        }
    }

    private fun setupInputBar() {
        binding.buttonSend.setOnClickListener {
            sendCurrentMessage()
        }

        binding.editMessage.doAfterTextChanged { text ->
            binding.buttonSend.isEnabled = !text.isNullOrBlank() && !viewModel.uiState.value.isLoading
        }

        binding.editMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendCurrentMessage()
                true
            } else {
                false
            }
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderState(state)
                }
            }
        }
    }

    private fun renderState(state: ChatUiState) {
        chatAdapter.submitMessages(state.messages, state.isLoading) {
            if (state.messages.isNotEmpty() || state.isLoading) {
                binding.recyclerMessages.scrollToPosition(chatAdapter.itemCount - 1)
            }
        }

        binding.progressOverlay.isVisible = false
        binding.demoBanner.isVisible = state.isDemoMode
        binding.buttonSend.isEnabled = !binding.editMessage.text.isNullOrBlank() && !state.isLoading
        binding.editMessage.isEnabled = !state.isLoading

        if (state.showApiKeyDialog && apiKeyDialog?.isShowing != true) {
            showApiKeyDialog()
        }

        state.errorMessage?.let { message ->
            showErrorSnackbar(message)
            viewModel.dismissError()
        }
    }

    private fun sendCurrentMessage() {
        val message = binding.editMessage.text?.toString().orEmpty()
        if (message.isBlank()) return

        binding.editMessage.text?.clear()
        viewModel.sendMessage(message)
    }

    private fun showErrorSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction(R.string.retry) {
                viewModel.retryLastResponse()
            }
            .show()
    }

    private fun showApiKeyDialog() {
        val input = TextInputEditText(requireContext()).apply {
            hint = getString(R.string.api_key_hint)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setPadding(48, 32, 48, 32)
        }

        apiKeyDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.api_key_dialog_title)
            .setMessage(R.string.api_key_dialog_message)
            .setView(input)
            .setPositiveButton(R.string.save) { _, _ ->
                viewModel.saveApiKey(input.text?.toString().orEmpty())
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                viewModel.dismissApiKeyDialog()
            }
            .setNeutralButton(R.string.get_api_key) { _, _ ->
                startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/apikey"))
                )
            }
            .setOnDismissListener {
                apiKeyDialog = null
                viewModel.dismissApiKeyDialog()
            }
            .show()
    }

    private fun showClearChatConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.clear_chat_title)
            .setMessage(R.string.clear_chat_message)
            .setPositiveButton(R.string.clear) { _, _ ->
                viewModel.clearChat()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
