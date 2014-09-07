package com.yahoo.dvictor.tip_calculator;

import java.text.DecimalFormat;

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
		// If the bill total changes, recalculate without having to click a button again.
		setupBillValueChangedListener();
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
			EditText etNewItem  = (EditText) findViewById(R.id.etBillTotal);
			String   billTxt    = etNewItem.getText().toString();
			double   bill       = Double.valueOf(billTxt).doubleValue();
			// Get Percentage Selected
			int      percentage = getPercentage(v);
			// Calculate
			double   tip        = bill*(((double)percentage)/100); // convert to fractional percentage (0.0 - 1.0) for multiplication
			// Display Result
			TextView tvTipTotal = (TextView) findViewById(R.id.tvTipTotal);
			tvTipTotal.setText(FORMAT_CURRENCY.format(tip));
			TextView tvTipPct   = (TextView) findViewById(R.id.tvTipPct);
			tvTipPct.setText(""+percentage+"%");
			// Remember the percentage last used for future edits.
			percent_lastUsed = percentage;
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
	
}
