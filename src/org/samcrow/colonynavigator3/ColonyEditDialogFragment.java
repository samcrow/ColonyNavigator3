package org.samcrow.colonynavigator3;

import org.samcrow.colonynavigator3.data.Colony;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

/**
 * A fragment that creates a dialog that can be used to edit a colony
 * 
 * @author Sam Crow
 *
 */
public class ColonyEditDialogFragment extends DialogFragment {
	

	public static ColonyEditDialogFragment newInstance(Colony colony) {
		final ColonyEditDialogFragment fragment = new ColonyEditDialogFragment();
		final Bundle args = new Bundle();
		
		args.putInt("colony_id", colony.getId());
		args.putBoolean("colony_visisted", colony.isVisited());
		args.putBoolean("colony_active", colony.isActive());
		
		fragment.setArguments(args);
		
		return fragment;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		final Bundle args = getArguments();
		final int colonyId = args.getInt("colony_id");
		final boolean colonyVisited = args.getBoolean("colony_visited");
		final boolean colonyActive = args.getBoolean("colony_active");
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
		
		builder.setTitle("Colony " + colonyId);
		
		// Inflate the view from the XML file
		final View view = getActivity().getLayoutInflater().inflate(R.layout.colony_edit, null);
		builder.setView(view);
		
		final CheckBox visitedBox = (CheckBox) view.findViewById(R.id.visited_checkbox);
		visitedBox.setChecked(colonyVisited);
		final CheckBox activeBox = (CheckBox) view.findViewById(R.id.active_checkbox);
		activeBox.setChecked(colonyActive);
		
		// Set up buttons
		builder.setPositiveButton(R.string.save_action, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO: Save colony
				
				ColonyEditDialogFragment.this.getDialog().dismiss();
			}
		});

		builder.setNegativeButton(R.string.cancel_action, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ColonyEditDialogFragment.this.getDialog().cancel();
			}
		});
		
		return builder.create();
	}

}
