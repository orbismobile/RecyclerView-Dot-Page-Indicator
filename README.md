<p align="center">
    <a target="_blank" href="https://jitpack.io/#orbismobile/RecyclerView-Dot-Page-Indicator"><img src="https://jitpack.io/v/orbismobile/RecyclerView-Dot-Page-Indicator.svg"></a>
</p>

<h1 align="center"> RecyclerView Dot Page Indicator </h1>

RecyclerView Dot Page Indicator is a widget that generate Dots Indicator per each page from your RecyclerView 
with a behaviour like a ViewPager conventional

# RecyclerDataObserver class
This class allows to listen to the change events that the recycler may have in case its adapter changes size.

# CustomPagerHelper class
This class extends from the PagerSnapHelper class, allowing me to hear the page change event (findTargetSnapPosition),
native to the pager, as well as giving gallery behavior to the recycler.

# DotPageIndicator class
This class contains all the assembly logic of paginate and paint for each dot.
