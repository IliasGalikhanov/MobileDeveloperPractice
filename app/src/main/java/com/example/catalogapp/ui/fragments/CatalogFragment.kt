package com.example.catalogapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.catalogapp.databinding.FragmentCatalogBinding
import com.example.catalogapp.ui.adapters.CatalogAdapter
import com.example.catalogapp.viewmodel.CatalogViewModel

/**
 * Fragment для отображения списка товаров
 */
class CatalogFragment : Fragment() {

    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!!

    // Используем activityViewModels для общего доступа к ViewModel между фрагментами
    private val viewModel: CatalogViewModel by activityViewModels()
    
    private lateinit var adapter: CatalogAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        observeViewModel()
    }

    /**
     * Настройка RecyclerView с адаптером
     */
    private fun setupRecyclerView() {
        adapter = CatalogAdapter { item ->
            // Навигация на экран деталей при клике на элемент
            // Safe Args автоматически генерирует класс CatalogFragmentDirections
            val action = CatalogFragmentDirections.actionCatalogToDetails(itemId = item.id)
            findNavController().navigate(action)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CatalogFragment.adapter
            
            // Оптимизация для фиксированного размера элементов
            setHasFixedSize(true)
        }
    }

    /**
     * Подписка на LiveData из ViewModel
     */
    private fun observeViewModel() {
        viewModel.catalogItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
