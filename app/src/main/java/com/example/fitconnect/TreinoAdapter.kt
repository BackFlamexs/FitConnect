package com.example.fitconnect

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Treino(
    val id: Int,
    val nome: String,
    val tagDia: String,
    val diaSemana: String,
    val detalhes: String
)

class TreinoAdapter(
    private val listaTreinos: List<Treino>,
    private val onDeletar: (Treino) -> Unit
) : RecyclerView.Adapter<TreinoAdapter.TreinoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TreinoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_treino, parent, false)
        return TreinoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TreinoViewHolder, position: Int) {
        val treino = listaTreinos[position]

        holder.tvNome.text = treino.nome
        holder.tvTagDia.text = treino.tagDia
        holder.tvDiaSemana.text = treino.diaSemana
        holder.tvDetalhes.text = treino.detalhes

        holder.btnVisualizar.setOnClickListener {
            val ctx = holder.itemView.context
            val intent = Intent(ctx, DetalheTreinoActivity::class.java)
            intent.putExtra("NOME_TREINO", treino.nome)
            intent.putExtra("TREINO_ID", treino.id)
            ctx.startActivity(intent)
        }

        holder.ivMaisOpcoes.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menu.add(0, 1, 0, "Editar treino")
            popup.menu.add(0, 2, 1, "Excluir treino")
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> {
                        val ctx = holder.itemView.context
                        val intent = Intent(ctx, EditarTreinoActivity::class.java)
                        intent.putExtra("TREINO_ID", treino.id)
                        intent.putExtra("NOME_TREINO", treino.nome)
                        ctx.startActivity(intent)
                        true
                    }
                    2 -> {
                        onDeletar(treino)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount(): Int = listaTreinos.size

    class TreinoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNome: TextView = itemView.findViewById(R.id.tv_nome_treino)
        val tvTagDia: TextView = itemView.findViewById(R.id.tv_tag_dia)
        val tvDiaSemana: TextView = itemView.findViewById(R.id.tv_dia_semana)
        val tvDetalhes: TextView = itemView.findViewById(R.id.tv_detalhes_treino)
        val btnVisualizar: Button = itemView.findViewById(R.id.btn_visualizar)
        val ivMaisOpcoes: ImageView = itemView.findViewById(R.id.iv_mais_opcoes)
    }
}
