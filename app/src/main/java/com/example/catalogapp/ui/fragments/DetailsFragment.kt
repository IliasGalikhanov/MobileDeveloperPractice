package com.example.catalogapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.catalogapp.databinding.FragmentDetailsBinding
import com.example.catalogapp.model.CatalogItem
import com.example.catalogapp.viewmodel.CatalogViewModel

class DetailsFragment : Fragment() {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private val args: DetailsFragmentArgs by navArgs()

    private val viewModel: CatalogViewModel by activityViewModels()
    
    private var currentItem: CatalogItem? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemId = args.itemId
        
        loadItemDetails(itemId)
        setupButtons()
        observeViewModel()
    }

    private fun loadItemDetails(itemId: Int) {
        currentItem = viewModel.getItemById(itemId)
        updateUI()
    }

    private fun updateUI() {
        currentItem?.let { item ->
            binding.apply {
                detailTitle.text = item.title
                detailDescription.text = item.description
                detailPrice.text = "${item.price.toInt()} тг"
                updateFavoriteButton(item.isFavorite)
            }
        }
    }

    private fun setupButtons() {
        binding.favoriteButton.setOnClickListener {
            currentItem?.let { item ->
                viewModel.toggleFavorite(item.id)
            }
        }

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewModel.catalogItems.observe(viewLifecycleOwner) { items ->
            currentItem = items.find { it.id == args.itemId }
            currentItem?.let { item ->
                updateFavoriteButton(item.isFavorite)
            }
        }
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        binding.favoriteButton.text = if (isFavorite) {
            "★ Убрать из избранного"
        } else {
            "☆ Добавить в избранное"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
