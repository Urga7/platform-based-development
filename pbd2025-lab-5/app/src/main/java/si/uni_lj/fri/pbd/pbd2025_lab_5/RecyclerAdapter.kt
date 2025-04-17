package si.uni_lj.fri.pbd.pbd2025_lab_5

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import si.uni_lj.fri.pbd.pbd2025_lab_5.databinding.CardLayoutBinding

class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.CardViewHolder?>() {

    private val titles = arrayOf("Mercury",
        "Venus",
        "Earth",
        "Mars",
        "Jupiter",
        "Saturn",
        "Uranus",
        "Neptune")
    private val details = arrayOf("The smallest planet",
        "The second brightest object in the night sky", "The only known to harbour life",
        "Named after the Roman god of war", "The largest planet in the Solar system",
        "Famous for its rings", "The coldest planet",
        "The farthest from the Sun")
    private val images = intArrayOf(R.drawable.mercury,
        R.drawable.venus,
        R.drawable.earth,
        R.drawable.mars,
        R.drawable.jupiter,
        R.drawable.saturn,
        R.drawable.uranus,
        R.drawable.neptune)

    inner class CardViewHolder(val binding: CardLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                Snackbar.make(
                    binding.root,
                    "Card at position $adapterPosition clicked",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = CardLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return titles.size
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.binding.itemTitle.text = titles[position]
        holder.binding.itemDetail.text = details[position]
        holder.binding.itemImage.setImageResource(images[position])
    }
}