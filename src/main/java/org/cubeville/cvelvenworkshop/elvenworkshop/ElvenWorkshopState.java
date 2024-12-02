package org.cubeville.cvelvenworkshop.elvenworkshop;

import org.cubeville.cvelvenworkshop.models.QuickChatMenu;
import org.cubeville.cvgames.models.PlayerState;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class ElvenWorkshopState extends PlayerState {
    public Boolean luckyBoots = false;
    public QuickChatMenu quickChatMenu;
    @Override
    public int getSortingValue() {
        return 0;
    }
}
