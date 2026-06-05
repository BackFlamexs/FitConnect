package com.example.fitconnect.feature.home

import com.example.fitconnect.R
import com.example.fitconnect.data.model.*
import com.example.fitconnect.core.network.RetrofitClient
import com.example.fitconnect.feature.treino.TreinosActivity
import com.example.fitconnect.feature.exercicio.GaleriaExerciciosActivity
import com.example.fitconnect.feature.arquivo.MeusArquivosActivity
import com.example.fitconnect.feature.feedback.HistoricoFeedbacksActivity
import com.example.fitconnect.feature.suporte.AjudaActivity
import com.example.fitconnect.feature.dashboard.DashboardProActivity
import com.example.fitconnect.feature.pagamento.PagamentoProActivity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var tvTituloSequencia: TextView
    private lateinit var tvBadgeDias: TextView

    // Números padrão (circulo cinza)
    private lateinit var tvNumSeg: TextView
    private lateinit var tvNumTer: TextView
    private lateinit var tvNumQua: TextView
    private lateinit var tvNumQui: TextView
    private lateinit var tvNumSex: TextView
    private lateinit var tvNumSab: TextView
    private lateinit var tvNumDom: TextView

    // Checks verdes (aparece quando tem treino)
    private lateinit var ivDiaSeg: ImageView
    private lateinit var ivDiaTer: ImageView
    private lateinit var ivDiaQua: ImageView
    private lateinit var ivDiaQui: ImageView
    private lateinit var ivDiaSex: ImageView
    private lateinit var ivDiaSab: ImageView
    private lateinit var ivDiaDom: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val tvNomeUsuario = findViewById<TextView>(R.id.tv_nome_usuario_home)
        val fotoPerfil = findViewById<View>(R.id.v_profile_home)
        val ivFotoHome = findViewById<ImageView>(R.id.iv_foto_home)

        val cardTreinos = findViewById<RelativeLayout>(R.id.card_treinos)
        val cardDashboardPro = findViewById<RelativeLayout>(R.id.card_dashboard_pro)
        val llDashboardBloqueado = findViewById<LinearLayout>(R.id.ll_dashboard_bloqueado)
        val cardAjuda = findViewById<RelativeLayout>(R.id.card_ajuda)
        val cardMeusArquivos = findViewById<RelativeLayout>(R.id.card_meus_arquivos)
        val cardFeedbacks = findViewById<RelativeLayout>(R.id.card_feedbacks)
        val cardGaleria = findViewById<RelativeLayout>(R.id.card_galeria)

        val navInicio = findViewById<LinearLayout>(R.id.nav_inicio)
        val navGaleria = findViewById<LinearLayout>(R.id.nav_galeria)
        val navAjuda = findViewById<LinearLayout>(R.id.nav_ajuda)
        val navMenu = findViewById<LinearLayout>(R.id.nav_menu)

        val isPro = Sessao.obterPro(this)
        val isPersonal = Sessao.obterAccountType(this) == "personal"

        if (isPro || isPersonal) {
            llDashboardBloqueado.visibility = View.GONE
            cardDashboardPro.setOnClickListener {
                startActivity(Intent(this, DashboardProActivity::class.java))
            }
        } else {
            llDashboardBloqueado.visibility = View.VISIBLE
            cardDashboardPro.setOnClickListener {
                startActivity(Intent(this, PagamentoProActivity::class.java))
            }
        }

        tvTituloSequencia = findViewById(R.id.tv_titulo_sequencia)
        tvBadgeDias = findViewById(R.id.tv_badge_dias)

        tvNumSeg = findViewById(R.id.tv_num_seg)
        tvNumTer = findViewById(R.id.tv_num_ter)
        tvNumQua = findViewById(R.id.tv_num_qua)
        tvNumQui = findViewById(R.id.tv_num_qui)
        tvNumSex = findViewById(R.id.tv_num_sex)
        tvNumSab = findViewById(R.id.tv_num_sab)
        tvNumDom = findViewById(R.id.tv_num_dom)

        ivDiaSeg = findViewById(R.id.iv_dia_seg)
        ivDiaTer = findViewById(R.id.iv_dia_ter)
        ivDiaQua = findViewById(R.id.iv_dia_qua)
        ivDiaQui = findViewById(R.id.iv_dia_qui)
        ivDiaSex = findViewById(R.id.iv_dia_sex)
        ivDiaSab = findViewById(R.id.iv_dia_sab)
        ivDiaDom = findViewById(R.id.iv_dia_dom)

        val nomeLogado = intent.getStringExtra("NOME_USUARIO") ?: Sessao.obterNomeUsuario(this)
        tvNomeUsuario.text = "Olá, $nomeLogado"
        carregarFotoPerfil(ivFotoHome)

        fotoPerfil.setOnClickListener { abrirMenu(nomeLogado) }
        navMenu.setOnClickListener { abrirMenu(nomeLogado) }
        navInicio.setOnClickListener { }
        navGaleria.setOnClickListener { startActivity(Intent(this, GaleriaExerciciosActivity::class.java)) }
        navAjuda.setOnClickListener { startActivity(Intent(this, AjudaActivity::class.java)) }

        cardTreinos.setOnClickListener { startActivity(Intent(this, TreinosActivity::class.java)) }
        cardAjuda.setOnClickListener { startActivity(Intent(this, AjudaActivity::class.java)) }
        cardMeusArquivos.setOnClickListener { startActivity(Intent(this, MeusArquivosActivity::class.java)) }
        cardFeedbacks.setOnClickListener { startActivity(Intent(this, HistoricoFeedbacksActivity::class.java)) }
        cardGaleria.setOnClickListener { startActivity(Intent(this, GaleriaExerciciosActivity::class.java)) }
    }

    override fun onResume() {
        super.onResume()
        carregarFotoPerfil(findViewById(R.id.iv_foto_home))
        carregarSequenciaSemanal()
    }

    private fun carregarFotoPerfil(ivFoto: ImageView) {
        val fotoUrl = Sessao.obterFotoUrl(this)
        if (fotoUrl.isNotEmpty()) {
            Glide.with(this)
                .load(fotoUrl)
                .centerCrop()
                .placeholder(R.drawable.img_avatar)
                .error(R.drawable.img_avatar)
                .into(ivFoto)
        } else {
            ivFoto.setImageResource(R.drawable.img_avatar)
        }
    }

    private fun carregarSequenciaSemanal() {
        val usuarioId = Sessao.obterUsuarioId(this)
        RetrofitClient.api.buscarFeedbacks("eq.$usuarioId").enqueue(object : Callback<List<FeedbackBanco>> {
            override fun onResponse(call: Call<List<FeedbackBanco>>, response: Response<List<FeedbackBanco>>) {
                if (response.isSuccessful) {
                    atualizarSequencia(response.body() ?: emptyList())
                }
            }
            override fun onFailure(call: Call<List<FeedbackBanco>>, t: Throwable) {}
        })
    }

    private fun atualizarSequencia(feedbacks: List<FeedbackBanco>) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val hoje = Calendar.getInstance()
        val diaSemanaHoje = hoje.get(Calendar.DAY_OF_WEEK)
        val diasAteSegunda = if (diaSemanaHoje == Calendar.SUNDAY) 6 else diaSemanaHoje - Calendar.MONDAY
        val inicioSemana = (hoje.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -diasAteSegunda)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val fimSemana = (inicioSemana.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, 6)
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59)
        }

        val diasFeitos = mutableSetOf<Int>()
        for (f in feedbacks) {
            try {
                val data = sdf.parse(f.criado_em.take(10)) ?: continue
                val cal = Calendar.getInstance().apply { time = data }
                if (!cal.before(inicioSemana) && !cal.after(fimSemana)) {
                    diasFeitos.add(cal.get(Calendar.DAY_OF_WEEK))
                }
            } catch (e: Exception) {}
        }

        // Mapa: dia Calendar → par (número TextView, check ImageView)
        val mapa = listOf(
            Triple(Calendar.MONDAY,    tvNumSeg, ivDiaSeg),
            Triple(Calendar.TUESDAY,   tvNumTer, ivDiaTer),
            Triple(Calendar.WEDNESDAY, tvNumQua, ivDiaQua),
            Triple(Calendar.THURSDAY,  tvNumQui, ivDiaQui),
            Triple(Calendar.FRIDAY,    tvNumSex, ivDiaSex),
            Triple(Calendar.SATURDAY,  tvNumSab, ivDiaSab),
            Triple(Calendar.SUNDAY,    tvNumDom, ivDiaDom)
        )
        for ((dia, tvNum, ivCheck) in mapa) {
            val feito = dia in diasFeitos
            tvNum.visibility = if (feito) View.GONE else View.VISIBLE
            ivCheck.visibility = if (feito) View.VISIBLE else View.GONE
        }

        val count = diasFeitos.size
        tvBadgeDias.text = "🔥 $count Dia${if (count != 1) "s" else ""}"

        // Sequência = dias feitos sem intervalo entre eles
        val ordemSemana = listOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
            Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY)
        val indices = diasFeitos.map { ordemSemana.indexOf(it) }.sorted()
        val isSequencia = count > 1 && indices.zipWithNext().all { (a, b) -> b == a + 1 }
        tvTituloSequencia.text = if (isSequencia) "Sequência Semanal" else "Treinos Semanal"
    }

    private fun abrirMenu(nomeUsuario: String) {
        val intent = Intent(this, MenuActivity::class.java)
        intent.putExtra("NOME_USUARIO", Sessao.obterNomeUsuario(this))
        startActivity(intent)
    }
}
