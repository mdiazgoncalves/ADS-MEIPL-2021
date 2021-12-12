<template>
  <HomeCardsGrid :cards="cards"/>
</template>

<script>
import HomeCardsGrid from "@/components/HomeCardsGrid";

export default {
  name: "Home",
  components: {HomeCardsGrid},
  setup() {
    return {
      cards: [
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
        {
          title: "Help",
          href: "/help",
          src: require("@/assets/img/home_cards/undraw_questions_re_1fy7.svg"),
          alt: "Help",
        },
      ],
    }
  },
}
</script>