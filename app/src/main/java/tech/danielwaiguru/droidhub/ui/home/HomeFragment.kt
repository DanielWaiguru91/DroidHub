package tech.danielwaiguru.droidhub.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import tech.danielwaiguru.droidhub.common.gone
import tech.danielwaiguru.droidhub.common.visible
import tech.danielwaiguru.droidhub.databinding.FragmentHomeBinding
import tech.danielwaiguru.droidhub.ui.adapter.FileUploadAdapter

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val fileAdapter: FileUploadAdapter by lazy { FileUploadAdapter() }
    private val homeViewModel: HomeViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        setupRecyclerView()
        homeViewModel.getFiles()
        subscribers()
    }
    private fun subscribers() {
        homeViewModel.loading.observe(viewLifecycleOwner,  { loading ->
            if (loading){
                binding.filesProgress.visible()
            }
            else {
                binding.filesProgress.gone()
            }
        })
        homeViewModel.files.observe(viewLifecycleOwner, {
            fileAdapter.submitList(it)
        })
    }
    private fun initListeners() {
        with(binding) {
            addFile.setOnClickListener { startUploadUi() }
        }
    }
    private fun setupRecyclerView() = binding.filesRecyclerView.apply {
        layoutManager = LinearLayoutManager(requireContext())
        setHasFixedSize(true)
        adapter = fileAdapter
    }
    private fun startUploadUi() {
        val action = HomeFragmentDirections.actionHomeFragmentToUploadFileFragment()
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}