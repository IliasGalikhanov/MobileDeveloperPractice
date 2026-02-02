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

/**
 * Fragment для отображения детальной информации о товаре
 */
class DetailsFragment : Fragment() {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    // Получение аргументов через Safe Args
    // Safe Args автоматически генерирует класс DetailsFragmentArgs
    private val args: DetailsFragmentArgs by navArgs()
    
    // Общая ViewModel с CatalogFragment
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
        
        // Получаем itemId из Safe Args
        val itemId = args.itemId
        
        loadItemDetails(itemId)
        setupButtons()
        observeViewModel()
    }

    /**
     * Загрузка информации о товаре
     */
    private fun loadItemDetails(itemId: Int) {
        currentItem = viewModel.getItemById(itemId)
        updateUI()
    }

    /**
     * Обновление UI с данными товара
     */
    private fun updateUI() {
        currentItem?.let { item ->
            binding.apply {
                detailTitle.text = item.title
                detailDescription.text = item.description
                detailPrice.text = "${item.price.toInt()} ₽"
                updateFavoriteButton(item.isFavorite)
            }
        }
    }

    /**
     * Настройка кнопок
     */
    private fun setupButtons() {
        // Кнопка "В избранное"
        binding.favoriteButton.setOnClickListener {
            currentItem?.let { item ->
                viewModel.toggleFavorite(item.id)
            }
        }

        // Кнопка "Назад"
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    /**
     * Наблюдение за изменениями в ViewModel
     */
    private fun observeViewModel() {
        viewModel.catalogItems.observe(viewLifecycleOwner) { items ->
            // Обновляем текущий товар при изменении списка
            currentItem = items.find { it.id == args.itemId }
            currentItem?.let { item ->
                updateFavoriteButton(item.isFavorite)
            }
        }
    }

    /**
     * Обновление состояния кнопки избранного
     */
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
