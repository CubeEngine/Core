package de.cubeisland.cubeengine.shout.announce.receiver;

import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.shout.announce.Announcement;
import de.cubeisland.cubeengine.shout.announce.AnnouncementManager;

import java.util.Queue;

public abstract class AbstractReceiver implements AnnouncementReceiver
{
    private final AnnouncementManager announcementManager;
    private Queue<Announcement> announcements;

    protected AbstractReceiver(AnnouncementManager announcementManager)
    {
        this.announcementManager = announcementManager;
    }

    public void setAllAnnouncements(Queue<Announcement> announcements)
    {
        this.announcements = announcements;
    }

    public Pair<Announcement, Integer> getNextDelayAndAnnouncement()
    {
        for (int x = 0; x < announcements.size(); x++)
        {
            Announcement announcement = announcements.poll();
            announcements.add(announcement);
            if (announcement.hasWorld(this.getWorld()))
            {
                return new Pair<Announcement, Integer>(announcement, (int)(announcement.getDelay() / announcementManager.getGreatestCommonDivisor(this)));
            }
        }
        return null;
    }

    public Queue<Announcement> getAllAnnouncements()
    {
        return announcements;
    }
}
