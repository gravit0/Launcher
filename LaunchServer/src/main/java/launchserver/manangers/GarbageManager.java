package launchserver.manangers;

import launchserver.NeedGarbageCollection;
import java.util.ArrayList;

public class GarbageManager {
    private static final ArrayList<NeedGarbageCollection> NEED_GARBARE_COLLECTION = new ArrayList<>();
    public static void registerNeedGC(NeedGarbageCollection gc)
    {
        NEED_GARBARE_COLLECTION.add(gc);
    }
    public static void gc()
    {
        for(NeedGarbageCollection gc : NEED_GARBARE_COLLECTION)
        {
            gc.garbageCollection();
        }
    }
}
