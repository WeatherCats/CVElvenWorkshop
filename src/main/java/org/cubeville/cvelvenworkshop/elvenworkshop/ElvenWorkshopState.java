package org.cubeville.cvelvenworkshop.elvenworkshop;

import org.cubeville.cvgames.models.PlayerState;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class ElvenWorkshopState extends PlayerState {
    public Boolean luckyBoots = false;
    @Override
    public int getSortingValue() {
        return 0;
    }
}
