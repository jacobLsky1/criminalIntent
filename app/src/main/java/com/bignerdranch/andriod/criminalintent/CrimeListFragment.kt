package com.bignerdranch.andriod.criminalintent

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ListAdapter
import android.text.format.DateFormat
import android.view.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import java.util.*

private const val TAG = "CrimeListFragment"


class CrimeListFragment : Fragment() {

    /**
     * Required interface for hosting activities
     *///defines that this fragment will implement the interface callbacks
    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)
    }
    private var callbacks: Callbacks? = null



    private lateinit var crimeRecyclerView: RecyclerView
    private var adapter: CrimeAdapter? = CrimeAdapter(emptyList())

    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeListViewModel::class.java)
    }

    // atach fragment to activity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setHasOptionsMenu(true)
        //when i use anything in the menu bar that differs from default
        // i have to override the FRAGMENT(for Activity its a diffrent prosses)
        // im working on and tell it that i have tampered with the menu bar
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)

        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view) as RecyclerView
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        crimeRecyclerView.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeListViewModel.crimeListLiveData.observe(viewLifecycleOwner,
            Observer { crimes ->
                crimes?.let {
                    Log.i(TAG, "Got crimes ${crimes.size}")
                    updateUI(crimes)
                }
            })
    }
// detach fragment to activity
    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    private fun updateUI(crimes: List<Crime>) {
        adapter = CrimeAdapter(crimes)
        crimeRecyclerView.adapter = adapter
    }

    companion object {
        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()
        }
    }

    private abstract class CrimeHolder(view: View) : RecyclerView.ViewHolder(view) {
        var crime = Crime()
        val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved)

    }

    private inner class NormalCrimeHolder(view: View) : CrimeHolder(view), View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime) {
            this.crime = crime
            titleTextView.text = this.crime.title
            var formatter:String = resources.getString(R.string.date_format)
            dateTextView.text = DateFormat.format(formatter, this.crime.date)//formating the date so it is more easely read
            solvedImageView.visibility = if (crime.isSolved) {
                View.VISIBLE
            } else {
                View.GONE
            }

        }

        override fun onClick(v: View) {
            callbacks?.onCrimeSelected(crime.id)

            //Toast.makeText(context, "${crime.title} pressed!", Toast.LENGTH_SHORT).show()
        }
    }


    private inner class CrimeAdapter(var crimes: List<Crime>)
        : ListAdapter<Crime,CrimeHolder>(DiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : CrimeHolder {
                        val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
                  return  NormalCrimeHolder(view)
        }

        override fun getItemCount(): Int = crimes.size

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = crimes[position]
            when (holder) {
                is NormalCrimeHolder -> holder.bind(crime)
                //is SeriousCrimeHolder -> holder.bind(crime)
                else -> throw IllegalArgumentException()
            }
        }

        override fun getItemViewType(position: Int): Int {
            val crime = crimes[position]
            //return when (crime.requiresPolice) {
               // true -> 1
               // else -> 0
           // }
            return 0
        }
    }


    private inner class DiffCallback(): DiffUtil.ItemCallback<Crime>() {

        override fun areItemsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {// to attach the new menu xml file and inflate it
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list,menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_crime -> {
                val crime=Crime()
                crimeListViewModel.addCrime(crime)
                callbacks?.onCrimeSelected(crime.id)
                true//the true is indicating that there is no further possessing to be done(the fun returns a boolean)
            }
            else -> return super.onOptionsItemSelected(item)
        }


    }
}
