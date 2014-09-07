package com.yahoo.dvictor.tip_calculator;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

public class TipCalculatorActivity extends Activity {

	// Private Member Variables
	private int percent_preset1  = 10;
	private int percent_preset2  = 15;
	private int percent_preset3  = 20;
	private int percent_custom   = 18;
	private int percent_lastUsed = -1;
	private int split_default    = 1;
	private static final DecimalFormat FORMAT_CURRENCY; // OR USE: NumberFormat.getCurrencyInstance(Locale.US);
	static{
		FORMAT_CURRENCY = new DecimalFormat();
		FORMAT_CURRENCY.setMaximumFractionDigits(2);
		FORMAT_CURRENCY.setMinimumFractionDigits(2);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tip_calculator);
		// Load any previous state
		loadData();
		// If the bill total changes, recalculate without having to click a button again.
		setupBillValueChangedListener();
		// Set split by default & listener for changes.
		EditText et = (EditText) findViewById(R.id.etSplit);
		et.setText(String.valueOf(split_default));
		setupSplitChangedListener();
		// Configure the customer % number picker settings that can't be set in the XML
		NumberPicker np = (NumberPicker) findViewById(R.id.npCustom);
		np.setMinValue(1);
		np.setMaxValue(99);
		np.setValue(percent_custom);
		// The number picker isn't a button, but we need to calculate value whenever it changes.
		setupCustomTipListener();
	}
	
	/** Setup a listener on the bill total field to detect user changes and auto-recalculate without having to click a button again. */
	private void setupBillValueChangedListener(){
		EditText et = (EditText) findViewById(R.id.etBillTotal);
		et.addTextChangedListener(new TextWatcher() {
		    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
		        // Fires right as the text is being changed (even supplies the range of text)
		    }
		    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		        // Fires right before text is changing
		    }
		    @Override public void afterTextChanged(Editable s) {
		    	// Fires after text done changing.
		    	// If they previously selected a button, reuse the last percentage used.  Else cannot calculate yet.
		        if(percent_lastUsed>=0) calculate(null);
		    }
		});	
	}

	/** Setup a listener on the split by field to detect user changes without having to click a button to recalculate. */
	private void setupSplitChangedListener(){
		final EditText et = (EditText) findViewById(R.id.etSplit);
		et.addTextChangedListener(new TextWatcher() {
		    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
		        // Fires right as the text is being changed (even supplies the range of text)
		    }
		    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		        // Fires right before text is changing
		    }
		    @Override public void afterTextChanged(Editable s) {
		    	// Fires after text done changing.
		    	// If they previously selected a calculate % button, we can recalcualte.  Else cannot calculate yet.
		        if(percent_lastUsed>=0) calculate(null);
		    }
		});	
	}
	
	/** Setup a listener on the custom percentage number picker to calculate the value whenever it changes. */
	private void setupCustomTipListener(){
		NumberPicker np = (NumberPicker) findViewById(R.id.npCustom);
		np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
			@Override public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				percent_custom = picker.getValue();
				calculate(picker);
			}
		});
	}
	
	/** Calculate the tip % based on the button pressed. */
	public void calculate(View v){
		try{
			// Get Bill Total
			EditText etBill     = (EditText) findViewById(R.id.etBillTotal);
			String   billTxt    = etBill.getText().toString();
			double   bill       = Double.valueOf(billTxt).doubleValue();
			// Get how many to split by
			EditText etSplit    = (EditText) findViewById(R.id.etSplit);
			String   splitTxt   = etSplit.getText().toString();
			double   split      = Double.valueOf(splitTxt).doubleValue();
			// Get Percentage Selected
			int      percentage = getPercentage(v);
			// Calculate
			double   tip        = (bill*(((double)percentage)/100))/split; // convert to fractional percentage (0.0 - 1.0) for multiplication
			// Display Result
			TextView tvTipTotal = (TextView) findViewById(R.id.tvTipTotal);
			tvTipTotal.setText(FORMAT_CURRENCY.format(tip));
			TextView tvTipPct   = (TextView) findViewById(R.id.tvTipPct);
			tvTipPct.setText(""+percentage+"%");
			// Remember the percentage last used for future edits.
			boolean percent_changed = percent_lastUsed != percentage;
			percent_lastUsed = percentage;			
			if(percent_changed) saveData(); // Save the change to state if it changed.
		}catch(NumberFormatException e){
			((TextView) findViewById(R.id.tvTipTotal)).setText("ERROR");
			((TextView) findViewById(R.id.tvTipPct)).setText("invalid");
		}
	}
	
	/** 
	 * Determine which percentage applies based on which button pressed.
	 * @param v - Button that the user clicked, or NULL for setting to last button used (or default).
	 * @return Whole percentage (00 - 100) as the user understands it.
	 **/
	private int getPercentage(View v){
		if(v==null) return percent_lastUsed;
		// Get the whole percentage corresponding to the button selected.
		Button b1 = (Button) findViewById(R.id.btPreset1);
		Button b2 = (Button) findViewById(R.id.btPreset2);
		Button b3 = (Button) findViewById(R.id.btPreset3);
		NumberPicker bx = (NumberPicker) findViewById(R.id.npCustom);
		     if(v.getId()==b1.getId()) return percent_preset1;
		else if(v.getId()==b2.getId()) return percent_preset2;
		else if(v.getId()==b3.getId()) return percent_preset3;
		else if(v.getId()==bx.getId()) return percent_custom;
		else throw new RuntimeException("Unexpected view: "+v.toString());
	}

	/** Save any custom settings to the persisted file -- tips.txt. */
	private void saveData(){
		// Pack up state data into Array List
		ArrayList<String> data = new ArrayList<>();
		data.add(String.valueOf(percent_custom  ));
		data.add(String.valueOf(percent_lastUsed));
		data.add(String.valueOf(percent_preset1 ));
		data.add(String.valueOf(percent_preset2 ));
		data.add(String.valueOf(percent_preset3 ));
		// Then write serialized data to file.
		File filesDir = getFilesDir();
		File file = new File(filesDir,"tips.txt");
		try{
			FileUtils.writeLines(file, data);
		}catch(IOException e){
			e.printStackTrace();
		}
	}	
	
	/** Read any custom settings from the persisted file (if any) -- tips.txt. */
	private void loadData(){
		// Read List as unserialized data
		ArrayList<String> data = null;
		File filesDir = getFilesDir();
		File file = new File(filesDir,"tips.txt");
		try{
			if(file.exists()) data = new ArrayList<String>(FileUtils.readLines(file));
		}catch(IOException e){
			e.printStackTrace();
		}
		// Unpack serialized data back into our members
		if(data!=null){
			try{
				if(data.size()>0) percent_custom   = Integer.valueOf(data.get(0)).intValue();
				if(data.size()>1) percent_lastUsed = Integer.valueOf(data.get(1)).intValue();
				if(data.size()>2) percent_preset1  = Integer.valueOf(data.get(2)).intValue();
				if(data.size()>3) percent_preset2  = Integer.valueOf(data.get(3)).intValue();
				if(data.size()>4) percent_preset3  = Integer.valueOf(data.get(4)).intValue();
			}catch(Exception e){
				// Just ignore and let it use remaining defaults, and it will save a new file next time.
			}
		}
	}
	
}
