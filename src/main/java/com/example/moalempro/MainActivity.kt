package com.example.moalempro

import android.app.*
import android.os.Bundle
import android.content.*
import android.graphics.Color
import android.net.Uri
import android.view.*
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {
    private lateinit var db: DB
    private lateinit var root: LinearLayout
    private val navy = Color.rgb(16,36,62)
    private val gold = Color.rgb(217,164,65)

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        db = DB(this)
        home()
    }

    private fun home() {
        root = LinearLayout(this); root.orientation=LinearLayout.VERTICAL; root.setBackgroundColor(Color.rgb(245,247,250))
        val head=TextView(this); head.text="مساعد المعلم Pro"; head.textSize=25f; head.setTextColor(Color.WHITE)
        head.gravity=Gravity.CENTER; head.setPadding(10,35,10,35); head.setBackgroundColor(navy); root.addView(head)
        val sub=TextView(this); sub.text="إدارة الطلاب • الحضور • المدفوعات • الاختبارات"; sub.textSize=15f; sub.gravity=Gravity.CENTER; sub.setPadding(8,18,8,18); root.addView(sub)
        val grid=LinearLayout(this); grid.orientation=LinearLayout.VERTICAL; grid.setPadding(18,10,18,10); root.addView(grid, LinearLayout.LayoutParams(-1,0,1f))
        val items=listOf("👨‍🎓 الطلاب","👥 المجموعات","📅 الحضور والواجب","💰 المدفوعات","📝 الاختبارات والدرجات","📊 التقارير","💾 النسخ الاحتياطي","⚙️ الإعدادات")
        items.chunked(2).forEach { pair ->
            val row=LinearLayout(this); row.orientation=LinearLayout.HORIZONTAL
            pair.forEach { t ->
                val v=button(t); row.addView(v, LinearLayout.LayoutParams(0,75,1f).apply{setMargins(6,6,6,6)})
                v.setOnClickListener { when(t.substring(2)) {
                    "الطلاب"->students(); "المجموعات"->groups(); "الحضور والواجب"->attendance()
                    "المدفوعات"->payments(); "الاختبارات والدرجات"->tests(); "التقارير"->reports()
                    "النسخ الاحتياطي"->backup(); "الإعدادات"->settings()
                }}
            }; grid.addView(row)
        }
        setContentView(root)
    }
    private fun button(s:String):Button { val b=Button(this); b.text=s; b.textSize=15f; return b }
    private fun edit(h:String):EditText { val e=EditText(this); e.hint=h; e.textSize=16f; e.setPadding(20,8,20,8); return e }
    private fun screen(title:String):LinearLayout {
        val l=LinearLayout(this); l.orientation=LinearLayout.VERTICAL; l.setPadding(18,18,18,18)
        val bar=LinearLayout(this); val back=button("‹"); back.setOnClickListener{home()}
        val tv=TextView(this); tv.text=title; tv.textSize=22f; tv.setTextColor(navy); tv.gravity=Gravity.CENTER_VERTICAL
        bar.addView(back,LinearLayout.LayoutParams(60,60)); bar.addView(tv,LinearLayout.LayoutParams(0,60,1f)); l.addView(bar); return l
    }
    private fun students() {
        val l=screen("الطلاب"); val name=edit("اسم الطالب"); val phone=edit("رقم الهاتف"); val group=edit("المجموعة")
        val save=button("حفظ الطالب"); l.addView(name);l.addView(phone);l.addView(group);l.addView(save)
        val list=LinearLayout(this);list.orientation=LinearLayout.VERTICAL;l.addView(list,LinearLayout.LayoutParams(-1,0,1f))
        fun refresh(){list.removeAllViews();db.students().forEach{ s-> val b=button("${s.name}  •  ${s.group}\n${s.phone}");b.setOnClickListener{studentCard(s.id)};list.addView(b)}}
        save.setOnClickListener{if(name.text.isNotBlank()){db.addStudent(name.text.toString(),phone.text.toString(),group.text.toString());name.text.clear();phone.text.clear();group.text.clear();refresh()}}
        refresh();setContentView(l)
    }
    private fun studentCard(id:Long){ val s=db.getStudent(id)?:return; val l=screen("ملف الطالب"); l.addView(TextView(this).apply{text="${s.name}\nالمجموعة: ${s.group}\nالهاتف: ${s.phone}\n\nالحضور: ${db.count("present",id)}\nالغياب: ${db.count("absent",id)}\nالمدفوع: ${db.paid(id)}";textSize=18f;setPadding(10,20,10,20)})
        val del=button("حذف الطالب");del.setOnClickListener{db.deleteStudent(id);students()};l.addView(del);setContentView(l)}
    private fun groups(){ val l=screen("المجموعات"); val e=edit("اسم المجموعة");val add=button("إضافة مجموعة");l.addView(e);l.addView(add);val list=LinearLayout(this);list.orientation=LinearLayout.VERTICAL;l.addView(list,LinearLayout.LayoutParams(-1,0,1f));fun r(){list.removeAllViews();db.groups().forEach{list.addView(TextView(this).apply{text="• $it";textSize=18f;setPadding(10,18,10,18)})}};add.setOnClickListener{if(e.text.isNotBlank()){db.addGroup(e.text.toString());e.text.clear();r()}};r();setContentView(l)}
    private fun attendance(){val l=screen("الحضور والواجب");val id=edit("رقم الطالب");val st=edit("present / absent");val hw=edit("الواجب: done / missed");val save=button("حفظ السجل");l.addView(id);l.addView(st);l.addView(hw);l.addView(save);save.setOnClickListener{db.att(id.text.toString().toLongOrNull()?:0,st.text.toString(),hw.text.toString());Toast.makeText(this,"تم الحفظ",Toast.LENGTH_SHORT).show()};setContentView(l)}
    private fun payments(){val l=screen("المدفوعات");val id=edit("رقم الطالب");val amount=edit("المبلغ");val note=edit("ملاحظات");val save=button("تسجيل الدفعة");l.addView(id);l.addView(amount);l.addView(note);l.addView(save);save.setOnClickListener{db.payment(id.text.toString().toLongOrNull()?:0,amount.text.toString().toDoubleOrNull()?:0.0,note.text.toString());Toast.makeText(this,"تم حفظ الدفعة",Toast.LENGTH_SHORT).show()};setContentView(l)}
    private fun tests(){val l=screen("الاختبارات والدرجات");val id=edit("رقم الطالب");val exam=edit("اسم الاختبار");val score=edit("الدرجة");val save=button("حفظ الدرجة");l.addView(id);l.addView(exam);l.addView(score);l.addView(save);save.setOnClickListener{db.test(id.text.toString().toLongOrNull()?:0,exam.text.toString(),score.text.toString().toDoubleOrNull()?:0.0);Toast.makeText(this,"تم حفظ الدرجة",Toast.LENGTH_SHORT).show()};setContentView(l)}
    private fun reports(){val l=screen("التقارير");l.addView(TextView(this).apply{text="إجمالي الطلاب: ${db.students().size}\nإجمالي المجموعات: ${db.groups().size}\nإجمالي المدفوعات: ${db.totalPaid()}\nآخر تحديث: ${SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.getDefault()).format(Date())}";textSize=19f;setPadding(10,20,10,20)});setContentView(l)}
    private fun backup(){val l=screen("النسخ الاحتياطي");val ex=button("تصدير نسخة احتياطية JSON");val im=button("استيراد نسخة احتياطية");l.addView(ex);l.addView(im);ex.setOnClickListener{val i=Intent(Intent.ACTION_CREATE_DOCUMENT);i.type="application/json";i.putExtra(Intent.EXTRA_TITLE,"moalem_backup.json");startActivityForResult(i,10)};im.setOnClickListener{val i=Intent(Intent.ACTION_OPEN_DOCUMENT);i.type="application/json";i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,11)};setContentView(l)}
    private fun settings(){val l=screen("الإعدادات");l.addView(TextView(this).apply{text="مساعد المعلم Pro v2.0\nقاعدة بيانات محلية • حفظ تلقائي • يعمل بدون إنترنت";textSize=18f;setPadding(10,20,10,20)});setContentView(l)}
    override fun onActivityResult(r:Int,c:Int,d:Intent?){super.onActivityResult(r,c,d);if(c!=RESULT_OK||d?.data==null)return;try{if(r==10)contentResolver.openOutputStream(d.data!!)?.use{it.write(db.export().toString().toByteArray())};else db.import(JSONObject(contentResolver.openInputStream(d.data!!)?.bufferedReader()!!.readText()));Toast.makeText(this,"تمت العملية بنجاح",Toast.LENGTH_SHORT).show()}catch(e:Exception){Toast.makeText(this,"تعذر تنفيذ العملية",Toast.LENGTH_LONG).show()}}
}

data class Student(val id:Long,val name:String,val phone:String,val group:String)

class DB(c:Context){
    private val p=c.getSharedPreferences("moalem_db",Context.MODE_PRIVATE)
    private var students=JSONArray(p.getString("students","[]"))
    private var groups=JSONArray(p.getString("groups","[]"))
    private var records=JSONArray(p.getString("records","[]"))
    private fun save(){p.edit().putString("students",students.toString()).putString("groups",groups.toString()).putString("records",records.toString()).apply()}
    fun addStudent(n:String,ph:String,g:String){students.put(JSONObject().put("id",System.currentTimeMillis()).put("name",n).put("phone",ph).put("group",g));save()}
    fun students():List<Student>{val a=mutableListOf<Student>();for(i in 0 until students.length()){val x=students.getJSONObject(i);a.add(Student(x.getLong("id"),x.getString("name"),x.getString("phone"),x.getString("group")))};return a}
    fun getStudent(id:Long)=students().find{it.id==id}
    fun deleteStudent(id:Long){val n=JSONArray();for(i in 0 until students.length())if(students.getJSONObject(i).getLong("id")!=id)n.put(students.getJSONObject(i));students=n;save()}
    fun addGroup(s:String){if(groups.toString().contains("\"$s\"").not())groups.put(s);save()}
    fun groups():List<String>{val a=mutableListOf<String>();for(i in 0 until groups.length())a.add(groups.getString(i));return a}
    fun att(id:Long,st:String,hw:String){records.put(JSONObject().put("type","attendance").put("id",id).put("status",st).put("hw",hw).put("date",System.currentTimeMillis()));save()}
    fun payment(id:Long,am:Double,n:String){records.put(JSONObject().put("type","payment").put("id",id).put("amount",am).put("note",n).put("date",System.currentTimeMillis()));save()}
    fun test(id:Long,e:String,s:Double){records.put(JSONObject().put("type","test").put("id",id).put("exam",e).put("score",s).put("date",System.currentTimeMillis()));save()}
    fun count(st:String,id:Long):Int{var n=0;for(i in 0 until records.length()){val x=records.getJSONObject(i);if(x.optString("type")=="attendance"&&x.optLong("id")==id&&x.optString("status")==st)n++};return n}
    fun paid(id:Long):Double{var n=0.0;for(i in 0 until records.length()){val x=records.getJSONObject(i);if(x.optString("type")=="payment"&&x.optLong("id")==id)n+=x.optDouble("amount")};return n}
    fun totalPaid():Double{var n=0.0;for(i in 0 until records.length())if(records.getJSONObject(i).optString("type")=="payment")n+=records.getJSONObject(i).optDouble("amount");return n}
    fun export()=JSONObject().put("version","2.0").put("students",students).put("groups",groups).put("records",records)
    fun import(o:JSONObject){students=o.optJSONArray("students")?:JSONArray();groups=o.optJSONArray("groups")?:JSONArray();records=o.optJSONArray("records")?:JSONArray();save()}
}
