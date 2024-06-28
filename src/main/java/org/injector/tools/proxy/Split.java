package org.injector.tools.proxy;

import org.injector.tools.payload.Payload;

import java.util.ArrayList;

public class Split {

    ArrayList<SplitType> list = new ArrayList<>();
    ArrayList<Integer> index;

    public ArrayList<Integer> getSplitIndex(String requestPayload) {
        return Payload.getSplitIndexes(requestPayload);
    }

    /**
     * Enum of type's of Split.
     * <br> # [instant_split] = request 1 -> request 2
     * <br> # [split] = request 1 -> delay -> request 2
     * <br> # [delay_split] = request 1 -> more delay -> request 2
     * <br> # [repeat_split] = request 1 + request 1 -> request 2
     * <br> # [reverse_split] = request 1 + request 2 -> request 2
     * <br> # [split-x] = request 1 + request 2 -> delay -> request 2
     * <br> # [x-split] = request 1 + request 2 -> request 2
     *
     * @author salem
     */
    public enum SplitType {
        instant_split, split, delay_split, repeat_split, reverse_split, split_x, x_split;

        @Override
        public String toString() {
            switch (this) {
                case instant_split:
                    return "[instant_split]";
                case split:
                    return "[split]";
                case delay_split:
                    return "[delay_split]";
                case repeat_split:
                    return "[repeat_split]";
                case reverse_split:
                    return "[reverse_split]";
                case split_x:
                    return "[split-x]";
                case x_split:
                    return "[x-split]";
                default:
                    return "split";
            }
        }
    }

    /**
     * <br> # [instant_split] = request 1 -> request 2
     * <br> # [split] = request 1 -> delay -> request 2
     * <br> # [delay_split] = request 1 -> more delay -> request 2
     * <br> # [repeat_split] = request 1 + request 1 -> request 2
     * <br> # [reverse_split] = request 1 + request 2 -> request 2
     * <br> # [split-x] = request 1 + request 2 -> delay -> request 2
     * <br> # [x-split] = request 1 + request 2 -> request 2
     *
     * @author salem
     */
    public enum SplitAction {
        send, send_next_now, send_next_delay2, send_it_again_now,
    }


}
