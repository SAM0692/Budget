package com.sam.accel.budget.database;

import android.content.Context;
import android.util.Log;

import com.sam.accel.budget.model.Budget;
import com.sam.accel.budget.model.MonthlySavings;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.Sort;

/**
 * Created by SAcevedoM on 04/10/2017.
 */

public class BudgetDatabaseManager {

    private Realm realm;

    public BudgetDatabaseManager(Context context) {
        Realm.init(context);
        realm = Realm.getDefaultInstance();
    }

    private int createNewId(RealmObject modelClass) {
        int newId;
        Number lastId = realm.where(modelClass.getClass()).max("id");
        if (lastId == null) {
            newId = 1;
        } else {
            newId = lastId.intValue() + 1;
        }
        return newId;
    }

    public void insertBudget(float income) {
        // CREATE A NEW ID
        int newId = createNewId(new Budget());
        Log.d("NEWID", "the new Budget id is: " + newId);
        realm.beginTransaction();
        Budget newBudget = realm.createObject(Budget.class, newId);
        newBudget.setCreationDate(new Date());
        newBudget.setBaseIncome(income);

        realm.commitTransaction();

        insertNewMonth(newBudget);
    }

    public void closeActiveBudget() {
        Budget activeBudget = selectActiveBudget();

        if (activeBudget != null) {
            realm.beginTransaction();

            activeBudget.setClosingDate(new Date());

            realm.commitTransaction();
        }
    }

    public void insertNewMonth(Budget activeBudget) {
        int newId = createNewId(new MonthlySavings());
        Log.d("NEWID", "the new MonthlySaving id is: " + newId);
        realm.beginTransaction();

        MonthlySavings newMonth = realm.createObject(MonthlySavings.class, newId);
        newMonth.setIdBudget(activeBudget.getId());
        newMonth.setIncome(activeBudget.getBaseIncome());
        newMonth.setDate(new Date());

        realm.commitTransaction();
    }

    public Budget selectActiveBudget() {
        Budget budget;

        realm.beginTransaction();

        budget = realm.where(Budget.class).isNull("closingDate").findFirst();

        realm.commitTransaction();

        return budget;
    }

    public MonthlySavings selectCurrentMonth(int idBudget) {
        MonthlySavings month;

        realm.beginTransaction();

        month = realm.where(MonthlySavings.class).equalTo("idBudget", idBudget)
                .findAllSorted("date", Sort.DESCENDING).first();

        realm.commitTransaction();

        return month;
    }
}
