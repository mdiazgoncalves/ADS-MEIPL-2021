<template>
  <div>
    <header>
      <HomeNavBar/>
    </header>
    <main>
      <HomeCardsGrid :cards="cards"/>
    </main>
  </div>
</template>

<script>
import HomeCardsGrid from "@/components/HomeCardsGrid";
import HomeNavBar from "@/components/HomeNavBar";

export default {
  name: "Home",
  components: {HomeNavBar, HomeCardsGrid},
  data() {
    return {
      cards: [
        {
          title: "Classes",
          href: "",
          src: require("@/assets/img/home_cards/undraw_software_engineer_lvl5.svg"),
          alt: "Classes",
        },
        {
          title: "Relationships",
          href: "",
          src: require("@/assets/img/home_cards/undraw_mind_map_re_nlb6.svg"),
          alt: "Relationships",
        },
        {
          title: "Individuals",
          href: "",
          src: require("@/assets/img/home_cards/undraw_hire_re_gn5j.svg"),
          alt: "Individuals",
        },
        {
          title: "Queries",
          href: "",
          src: require("@/assets/img/home_cards/undraw_web_search_re_efla.svg"),
          alt: "Queries",
        },
        {
          title: "Branches",
          href: "",
          src: require("@/assets/img/home_cards/undraw_version_control_re_mg66.svg"),
          alt: "Branches",
        },
        {
          title: "Help",
          href: "",
          src: require("@/assets/img/home_cards/undraw_questions_re_1fy7.svg"),
          alt: "Help",
        },
      ]
    }
  },
}
</script>

<style scoped>

header {
  display: flex;
  justify-content: center;
}

main {
  margin-top: 32px;
}

</style>