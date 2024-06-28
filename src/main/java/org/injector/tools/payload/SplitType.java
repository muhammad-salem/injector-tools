package org.injector.tools.payload;

/*
 * 	# [instant_split] = request 1 -> request 2
	# [split] = request 1 -> delay -> request 2
	# [delay_split] = request 1 -> more delay -> request 2
	# [repeat_split] = request 1 + request 1 -> request 2
	# [reverse_split] = request 1 + request 2 -> request 2
	# [split-x] = request 1 + request 2 -> delay -> request 2
	# [x-split] = request 1 + request 2 -> request 2
 */
public enum SplitType {
    Split,
    Delay_Split,
    Instant_Split,
    Repeat_Split,
    Reverse_Split,
    Split_X,
    X_Split,
    NON
}