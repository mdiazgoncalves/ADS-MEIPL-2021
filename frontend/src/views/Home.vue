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
      },
      {
        title: "Relationships",
        href: "/relationships",
        src: require("@/assets/img/home_cards/undraw_mind_map_re_nlb6.svg"),
        alt: "Relationships",
      },
      {
        title: "Individuals",
        href: "/individuals",
        src: require("@/assets/img/home_cards/undraw_hire_re_gn5j.svg"),
        alt: "Individuals",
      },
      {
        title: "Queries",
        href: "/queries",
        src: require("@/assets/img/home_cards/undraw_web_search_re_efla.svg"),
        alt: "Queries",
      },
      {
        title: "Branches",
        href: "/branches",
        src: require("@/assets/img/home_cards/undraw_version_control_re_mg66.svg"),
        alt: "Branches",
      },
    ] : [
      {
        title: "Classes",
        href: "/classes",
        src: require("@/assets/img/home_cards/undraw_software_engineer_lvl5.svg"),
        alt: "Classes",
      },
      {
        title: "Relationships",
        href: "/relationships",
        src: require("@/assets/img/home_cards/undraw_mind_map_re_nlb6.svg"),
        alt: "Relationships",
      },
      {
        title: "Individuals",
        href: "/individuals",
        src: require("@/assets/img/home_cards/undraw_hire_re_gn5j.svg"),
        alt: "Individuals",
      },
      {
        title: "Queries",
        href: "/queries",
        src: require("@/assets/img/home_cards/undraw_web_search_re_efla.svg"),
        alt: "Queries",
      },
    ])

    return {
      cards,
    }
  },
}
</script>