package de.cubeisland.cubeengine.auctions;

import de.cubeisland.cubeengine.auctions.auction.Auction;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Faithcaio
 */
public enum Sorter
{
    ID, PRICE, DATE, QUANTITY;
    private static final Comparator compareId;
    private static final Comparator comparePrice;
    private static final Comparator compareDate;
    private static final Comparator compareQuantity;

    static
    {
        compareId = new Comparator()
        {
            public int compare(Object a1, Object a2)
            {
                if (((Auction) a2).getKey() <= ((Auction) a1).getKey())
                {
                    return 1;
                }
                //else
                return -1;
            }
        };
        comparePrice = new Comparator()
        {
            public int compare(Object a1, Object a2)
            {
                if (((Auction) a2).getBids().peek().getAmount() <= ((Auction) a1).getBids().peek().getAmount())
                {
                    return 1;
                }
                //else
                return -1;
            }
        };
        compareDate = new Comparator()
        {
            public int compare(Object a1, Object a2)
            {
                if (((Auction) a2).getAuctionEnd() <= ((Auction) a1).getAuctionEnd())
                {
                    return 1;
                }
                //else
                return -1;
            }
        };
        compareQuantity = new Comparator()
        {
            public int compare(Object a1, Object a2)
            {
                if (((Auction) a1).getItem().getAmount() <= ((Auction) a2).getItem().getAmount())
                {
                    return 1;
                }
                //else
                return -1;
            }
        };
    }

    /**
     * Sorts auctionlist
     *
     * @param auctionlist
     * @param type: id | price | date | quantity
     */
    public void sortAuction(List<Auction> auctionlist)
    {
        if (this == Sorter.ID)
        {
            Collections.sort(auctionlist, compareId);
        }
        if (this == Sorter.PRICE)
        {
            Collections.sort(auctionlist, comparePrice);
        }
        if (this == Sorter.DATE)
        {
            Collections.sort(auctionlist, compareDate);
        }
        if (this == Sorter.QUANTITY)
        {
            Collections.sort(auctionlist, compareQuantity);
        }
    }

    /**
     * Sorts auctionlist
     *
     * @param type: id | price | date | quantity
     * @param quantity: filter low quantity
     */
    public void sortAuction(List<Auction> auctionlist, int quantity)
    {
        this.sortAuction(auctionlist);

        if (this == Sorter.QUANTITY)
        {
            if (auctionlist.isEmpty())
            {
                return;
            }
            while (auctionlist.get(auctionlist.size() - 1).getItem().getAmount() < quantity)
            {
                auctionlist.remove(auctionlist.size() - 1);
                if (auctionlist.isEmpty())
                {
                    return;
                }
            }
        }
    }
}