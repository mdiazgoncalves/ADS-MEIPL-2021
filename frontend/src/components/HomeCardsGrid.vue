<template>
  <section id="home-cards">
    <div class="grid-container">
      <HomeCard v-for="(card, index) in cards" :key="index" :title="card.title" :src="card.src" :alt="card.alt" :href="card.href" :external="card.external" />
    </div>
  </section>
</template>

<script>
import HomeCard from "@/components/HomeCard";

export default {
  name: "HomeCardsGrid",
  props: {
    cards: Array,
  },
  components: {
    HomeCard,
  }
}
</script>

<style scoped>
#home-cards {
  width: 70%;
  margin: 0 auto;
}

.grid-container {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}
</style>