<template>
  <HomeCardsGrid :cards="cards"/>
</template>

<script>
import HomeCardsGrid from "@/components/HomeCardsGrid";
import {computed} from "vue";
import {useStore} from "vuex";

export default {
  name: "Home",
  components: {HomeCardsGrid},
  setup() {
    const store = useStore()
    const cards = computed(() => store.getters.token ? [
      {
        title: "Classes",
        href: "/classes",
        src: require("@/assets/img/home_cards/undraw_software_engineer_lvl5.svg"),
        alt: "Classes",
        external: false,
      },
      {
        title: "Relationships",
        href: "/relationships",
        src: require("@/assets/img/home_cards/undraw_mind_map_re_nlb6.svg"),
        alt: "Relationships",
        external: false,
      },
      {
        title: "Individuals",
        href: "/individuals",
        src: require("@/assets/img/home_cards/undraw_hire_re_gn5j.svg"),
        alt: "Individuals",
        external: false,
      },
      {
        title: "Queries",
        href: "/queries",
        src: require("@/assets/img/home_cards/undraw_web_search_re_efla.svg"),
        alt: "Queries",
        external: false,
      },
      {
        title: "Branches",
        href: "/branches",
        src: require("@/assets/img/home_cards/undraw_version_control_re_mg66.svg"),
        alt: "Branches",
        external: false,
      },
      {
        title: "Visualize",
        href: `${process.env.VUE_APP_WEBVOWL}${store.getters.branch ? `?branch=${store.getters.branch}` : "/"}`,
        src: require("@/assets/img/home_cards/undraw_goals_re_lu76.svg"),
        alt: "Visualize",
        external: true,
      },
    ] : [
      {
        title: "Classes",
        href: "/classes",
        src: require("@/assets/img/home_cards/undraw_software_engineer_lvl5.svg"),
        alt: "Classes",
        external: false,
      },
      {
        title: "Relationships",
        href: "/relationships",
        src: require("@/assets/img/home_cards/undraw_mind_map_re_nlb6.svg"),
        alt: "Relationships",
        external: false,
      },
      {
        title: "Individuals",
        href: "/individuals",
        src: require("@/assets/img/home_cards/undraw_hire_re_gn5j.svg"),
        alt: "Individuals",
        external: false,
      },
      {
        title: "Queries",
        href: "/queries",
        src: require("@/assets/img/home_cards/undraw_web_search_re_efla.svg"),
        alt: "Queries",
        external: false,
      },
      {
        title: "Visualize",
        href: `${process.env.VUE_APP_WEBVOWL}${store.getters.branch ? `?branch=${store.getters.branch}` : "/"}`,
        src: require("@/assets/img/home_cards/undraw_goals_re_lu76.svg"),
        alt: "Visualize",
        external: true,
      },
    ])

    return {
      cards,
    }
  },
}
</script>