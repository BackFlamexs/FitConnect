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
import java.util.Calendar

data class Treino(
    val id: Int,
    val nome: String,
    val tagDia: String,
    val diaSemana: String,
    val detalhes: String,
    val concluido: Boolean = false
)

class TreinoAdapter(
    private val listaTreinos: List<Treino>,
    private val onDeletar: (Treino) -> Unit,
    private val permitirGerenciar: Boolean = true
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
        holder.tvRelacaoDia.text = obterRelacaoDia(treino.diaSemana)
        holder.tvDetalhes.text = if (treino.concluido && !permitirGerenciar) {
            "${treino.detalhes} - Treino concluido"
        } else {
            treino.detalhes
        }

        if (treino.concluido && !permitirGerenciar) {
            holder.tvTagDia.text = "CONCLUIDO"
            holder.btnVisualizar.text = "Treino concluido"
            holder.btnVisualizar.isEnabled = false
            holder.btnVisualizar.alpha = 0.65f
        } else {
            holder.btnVisualizar.text = "Visualizar"
            holder.btnVisualizar.isEnabled = true
            holder.btnVisualizar.alpha = 1f
        }

        holder.btnVisualizar.setOnClickListener {
            if (treino.concluido && !permitirGerenciar) return@setOnClickListener
            val ctx = holder.itemView.context
            val intent = Intent(ctx, DetalheTreinoActivity::class.java)
            intent.putExtra("NOME_TREINO", treino.nome)
            intent.putExtra("TREINO_ID", treino.id)
            intent.putExtra("DETALHES_TREINO", treino.detalhes)
            ctx.startActivity(intent)
        }

        holder.ivMaisOpcoes.visibility = if (permitirGerenciar) View.VISIBLE else View.GONE
        holder.ivMaisOpcoes.setOnClickListener { view ->
            if (!permitirGerenciar) return@setOnClickListener
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
                        intent.putExtra("TAG_DIA", treino.tagDia)
                        intent.putExtra("DIA_SEMANA", treino.diaSemana)
                        intent.putExtra("DETALHES_TREINO", treino.detalhes)
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
        val tvRelacaoDia: TextView = itemView.findViewById(R.id.tv_relacao_dia)
        val tvDetalhes: TextView = itemView.findViewById(R.id.tv_detalhes_treino)
        val btnVisualizar: Button = itemView.findViewById(R.id.btn_visualizar)
        val ivMaisOpcoes: ImageView = itemView.findViewById(R.id.iv_mais_opcoes)
    }

    private fun obterRelacaoDia(diaSemana: String): String {
        val diaTreino = when (normalizarDia(diaSemana)) {
            "SEGUNDA-FEIRA" -> Calendar.MONDAY
            "TERCA-FEIRA" -> Calendar.TUESDAY
            "QUARTA-FEIRA" -> Calendar.WEDNESDAY
            "QUINTA-FEIRA" -> Calendar.THURSDAY
            "SEXTA-FEIRA" -> Calendar.FRIDAY
            "SABADO" -> Calendar.SATURDAY
            "DOMINGO" -> Calendar.SUNDAY
            else -> return ""
        }

        val hoje = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val diferenca = (diaTreino - hoje + 7) % 7
        return when (diferenca) {
            0 -> " - Hoje"
            1 -> " - Amanha"
            6 -> " - Ontem"
            else -> ""
        }
    }

    private fun normalizarDia(valor: String): String {
        return valor
            .uppercase()
            .replace("Á", "A")
            .replace("À", "A")
            .replace("Â", "A")
            .replace("Ã", "A")
            .replace("É", "E")
            .replace("Ê", "E")
            .replace("Í", "I")
            .replace("Ó", "O")
            .replace("Ô", "O")
            .replace("Õ", "O")
            .replace("Ú", "U")
            .replace("Ç", "C")
    }
}
